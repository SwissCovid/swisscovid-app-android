/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.networking

import android.content.Context
import androidx.work.*
import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.ExponentialDistribution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.internal.logger.Logger
import org.dpppt.android.sdk.models.ExposeeAuthMethodAuthorization
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class FakeWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

	companion object {

		private const val TAG = "FakeWorker"
		const val WORK_TAG = "ch.admin.bag.dp3t.FakeWorker"
		private const val WORK_NAME_PING = "ch.admin.bag.dp3t.FakeWorker.PING"
		private const val WORK_NAME_PONG = "ch.admin.bag.dp3t.FakeWorker.PONG"
		private const val ARG_WORK_NAME = "workname"

		private const val FAKE_AUTH_CODE = "000000000000"
		private const val FACTOR_HOUR_MILLIS = 60 * 60 * 1000L
		private const val FACTOR_DAY_MILLIS = 24 * FACTOR_HOUR_MILLIS
		private const val MAX_DELAY_HOURS: Long = 48
		@JvmField
		val SAMPLING_RATE = if (BuildConfig.FLAVOR == "dev") 1.0f else 0.2f
		@JvmField
		var clock: Clock = ClockImpl()

		@JvmStatic
		fun safeStartFakeWorker(context: Context, customClock: Clock) {
			clock = customClock
			safeStartFakeWorker(context)
		}

		@JvmStatic
		fun safeStartFakeWorker(context: Context) {
			val secureStorage = SecureStorage.getInstance(context)
			var t_dummy = SecureStorage.getInstance(context).tDummy
			if (t_dummy == -1L) {
				t_dummy = clock.currentTimeMillis() + clock.syncInterval()
				secureStorage.tDummy = t_dummy
			}
			scheduleFakeWorker(context, t_dummy, secureStorage.scheduledFakeWorkerName ?: WORK_NAME_PING)
		}

		private fun scheduleFakeWorker(context: Context, t_dummy: Long, workName: String) {
			val secureStorage = SecureStorage.getInstance(context)
			secureStorage.scheduledFakeWorkerName = workName
			val now = clock.currentTimeMillis()
			val executionDelay = max(0L, t_dummy - now)
			val executionDelayDays = executionDelay.toDouble() / FACTOR_DAY_MILLIS
			Logger.d(TAG, "scheduled for execution in $executionDelayDays days")
			val constraints = Constraints.Builder()
				.setRequiredNetworkType(NetworkType.CONNECTED)
				.build()
			val fakeWorker = OneTimeWorkRequest.Builder(FakeWorker::class.java)
				.setConstraints(constraints)
				.setInitialDelay(executionDelay, TimeUnit.MILLISECONDS)
				.addTag(WORK_TAG)
				.setInputData(workDataOf(ARG_WORK_NAME to workName))
				.build()
			WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, fakeWorker)
		}

		@JvmStatic
		fun stop(context: Context) {
			WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
		}
	}

	override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
		val now = clock.currentTimeMillis()
		val secureStorage = SecureStorage.getInstance(applicationContext)
		var t_dummy = secureStorage.tDummy
		if (t_dummy < 0) {
			//if t_dummy < 0 because of some weird state, we reset it
			t_dummy = now + clock.syncInterval()
		}
		//to make sure we can still write the EncryptedSharedPreferences, we always write the value back
		secureStorage.tDummy = t_dummy
		while (t_dummy < now && isActive) {
			Logger.d(TAG, "start")
			// only do request if it was planned to do in the last 48h
			if (t_dummy >= now - FACTOR_HOUR_MILLIS * MAX_DELAY_HOURS) {
				DP3T.addWorkerStartedToHistory(applicationContext, "fake")
				val success = executeFakeRequest(applicationContext)
				if (success) {
					Logger.d(TAG, "finished with success")
				} else {
					Logger.e(TAG, "failed")
					scheduleNext(t_dummy)
					Result.failure()
				}
			} else {
				Logger.d(TAG, "outdated request is dropped.")
			}
			t_dummy += clock.syncInterval()
			secureStorage.tDummy = t_dummy
		}
		scheduleNext(t_dummy)
		Result.success()
	}

	private fun scheduleNext(t_dummy: Long) {
		val currentWorkName = inputData.getString(ARG_WORK_NAME)
		val nextWorkName = if (currentWorkName == WORK_NAME_PING) WORK_NAME_PONG else WORK_NAME_PING
		scheduleFakeWorker(applicationContext, t_dummy, nextWorkName)
	}

	private fun executeFakeRequest(context: Context): Boolean {
		return try {
			val authCodeRepository = AuthCodeRepository(context)
			val accessTokenResponse = authCodeRepository.getAccessTokenSync(AuthenticationCodeRequestModel(FAKE_AUTH_CODE, 1))
			val accessToken = accessTokenResponse.accessToken
			val countdownLatch = CountDownLatch(1)
			val error = AtomicBoolean(false)
			DP3T.sendFakeInfectedRequest(context, ExposeeAuthMethodAuthorization(getAuthorizationHeader(accessToken)),
				{
					countdownLatch.countDown()
				},
				{
					error.set(true)
					countdownLatch.countDown()
				}
			)
			countdownLatch.await()
			!error.get()
		} catch (e: IOException) {
			Logger.e(TAG, "fake request failed", e)
			false
		} catch (e: ResponseError) {
			Logger.e(TAG, "fake request failed", e)
			false
		} catch (e: InterruptedException) {
			Logger.e(TAG, "fake request failed", e)
			false
		}
	}

	private fun getAuthorizationHeader(accessToken: String): String {
		return "Bearer $accessToken"
	}

	interface Clock {
		fun syncInterval(): Long
		fun currentTimeMillis(): Long
	}

	class ClockImpl : Clock {
		override fun syncInterval(): Long {
			val newDelayDays = ExponentialDistribution.sampleFromStandard() / SAMPLING_RATE
			return (newDelayDays * FACTOR_DAY_MILLIS).toLong()
		}

		override fun currentTimeMillis(): Long {
			return System.currentTimeMillis()
		}
	}

}
