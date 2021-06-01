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

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.debug.DebugFragment
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.NotificationUtil
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.backend.SignatureException
import org.dpppt.android.sdk.internal.history.HistoryDatabase
import org.dpppt.android.sdk.internal.history.HistoryEntry
import org.dpppt.android.sdk.internal.history.HistoryEntryType
import org.dpppt.android.sdk.internal.logger.Logger
import java.io.IOException
import java.util.concurrent.TimeUnit

class ConfigWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

	companion object {
		private const val TAG = "ConfigWorker"
		private const val WORK_NAME = "ch.admin.bag.dp3t.ConfigWorker"

		private const val REPEAT_INTERVAL_CONFIG_HOURS = 6L
		private const val MAX_AGE_OF_CONFIG_FOR_RELOAD_AT_APP_START = 12 * 60 * 60 * 1000L //12h

		@JvmStatic
		fun scheduleConfigWorkerIfOutdated(context: Context) {
			val secureStorage = SecureStorage.getInstance(context)
			if (secureStorage.lastConfigLoadSuccess < System.currentTimeMillis() - MAX_AGE_OF_CONFIG_FOR_RELOAD_AT_APP_START ||
				secureStorage.lastConfigLoadSuccessAppVersion != BuildConfig.VERSION_CODE ||
				secureStorage.lastConfigLoadSuccessSdkInt != Build.VERSION.SDK_INT
			) {
				val constraints = Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.build()
				val periodicWorkRequest =
					PeriodicWorkRequest.Builder(ConfigWorker::class.java, REPEAT_INTERVAL_CONFIG_HOURS, TimeUnit.HOURS)
						.setConstraints(constraints)
						.build()
				val workManager = WorkManager.getInstance(context)
				workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest)
			}
		}

		@Throws(IOException::class, ResponseError::class, SignatureException::class)
		private suspend fun loadConfig(context: Context) {
			val configRepository = ConfigRepository(context)
			val config = configRepository.getConfig(context)

			DP3T.setMatchingParameters(
				context,
				config.sdkConfig.lowerThreshold, config.sdkConfig.higherThreshold,
				config.sdkConfig.factorLow, config.sdkConfig.factorHigh,
				config.sdkConfig.triggerThreshold
			)

			val secureStorage = SecureStorage.getInstance(context)
			secureStorage.doForceUpdate = config.doForceUpdate
			secureStorage.setWhatToDoPositiveTestTexts(config.whatToDoPositiveTestTexts)

			if (config.infoBox != null) {
				secureStorage.infoBoxCollection = config.infoBox
				secureStorage.hasInfobox = true
			} else {
				secureStorage.hasInfobox = false
			}

			secureStorage.setTestInformationUrls(config.testInformationUrls)

			secureStorage.testLocations = config.testLocations
			secureStorage.interopCountries = config.interOpsCountries

			val forceUpdate = secureStorage.doForceUpdate
			if (forceUpdate) {
				if (!secureStorage.forceUpdateLiveData.hasObservers()) {
					showNotification(context)
				}
			} else {
				cancelNotification(context)
			}
		}

		private fun showNotification(context: Context) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				NotificationUtil.createNotificationChannel(context)
			}
			val packageName = context.packageName
			val intent = Intent(Intent.ACTION_VIEW)
			intent.data = Uri.parse("market://details?id=$packageName")
			val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
			val notification = NotificationCompat.Builder(context, NotificationUtil.NOTIFICATION_CHANNEL_ID)
				.setContentTitle(context.getString(R.string.force_update_title))
				.setContentText(context.getString(R.string.force_update_text))
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setSmallIcon(R.drawable.ic_begegnungen)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.build()
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			if(DebugFragment.EXISTS){
				HistoryDatabase.getInstance(context).addEntry(
					HistoryEntry(
						HistoryEntryType.NOTIFICATION, "Showing update required notification", false,
						System.currentTimeMillis()
					)
				)
			}
			notificationManager.notify(NotificationUtil.NOTIFICATION_ID_UPDATE, notification)
		}

		private fun cancelNotification(context: Context) {
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.cancel(NotificationUtil.NOTIFICATION_ID_UPDATE)
		}
	}

	override suspend fun doWork(): Result {
		Logger.d(TAG, "started")
		DP3T.addWorkerStartedToHistory(applicationContext, "config")
		try {
			loadConfig(applicationContext)
		} catch (e: IOException) {
			Logger.e(TAG, "failed", e)
			return Result.failure()
		} catch (e: ResponseError) {
			Logger.e(TAG, "failed", e)
			return Result.failure()
		} catch (e: SignatureException) {
			Logger.e(TAG, "failed", e)
			return Result.failure()
		}
		Logger.d(TAG, "finished with success")
		return Result.success()
	}

}