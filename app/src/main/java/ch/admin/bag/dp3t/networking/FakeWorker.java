/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.networking;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.models.ExposeeAuthMethodAuthorization;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.networking.errors.ResponseError;
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel;
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeResponseModel;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.ExponentialDistribution;

public class FakeWorker extends Worker {

	private static final String TAG = "FakeWorker";
	private static final String WORK_TAG = "ch.admin.bag.dp3t.FakeWorker";
	private static final String FAKE_AUTH_CODE = "000000000000";

	private static final long FACTOR_HOUR_MILLIS = 60 * 60 * 1000L;
	private static final long FACTOR_DAY_MILLIS = 24 * FACTOR_HOUR_MILLIS;
	private static final long MAX_DELAY_HOURS = 48;
	private static final float SAMPLING_RATE = BuildConfig.FLAVOR.equals("dev") ? 1.0f : 0.2f;
	private static final String KEY_T_DUMMY = "KEY_T_DUMMY";

	public static void safeStartFakeWorker(Context context) {
		long t_dummy = SecureStorage.getInstance(context).getTDummy();
		if (t_dummy == -1){
			t_dummy = System.currentTimeMillis() + syncInterval();
			SecureStorage.getInstance(context).setTDummy(t_dummy);
		}
		startFakeWorker(context, ExistingWorkPolicy.KEEP, t_dummy);
	}

	private static void startFakeWorker(Context context, ExistingWorkPolicy policy, long t_dummy) {

		long now = System.currentTimeMillis();
		long executionDelay = Math.max(0L, t_dummy - now);
		double executionDelayDays = (double) executionDelay / FACTOR_DAY_MILLIS;

		Logger.d(TAG, "scheduled for execution in " + executionDelayDays + " days");

		Constraints constraints = new Constraints.Builder()
				.setRequiredNetworkType(NetworkType.CONNECTED)
				.build();

		OneTimeWorkRequest fakeWorker = new OneTimeWorkRequest.Builder(FakeWorker.class)
				.setConstraints(constraints)
				.setInitialDelay(executionDelay, TimeUnit.MILLISECONDS)
				.setInputData(new Data.Builder().putLong(KEY_T_DUMMY, t_dummy).build())
				.build();

		WorkManager.getInstance(context).enqueueUniqueWork(WORK_TAG, policy, fakeWorker);
	}

	public FakeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public ListenableWorker.Result doWork() {
		long now = System.currentTimeMillis();
		long t_dummy = getInputData().getLong(KEY_T_DUMMY, now);
		while (t_dummy < now) {
			Logger.d(TAG, "start");
			// only do request if it was planned to do in the last 48h
			if (t_dummy >= now - FACTOR_HOUR_MILLIS * MAX_DELAY_HOURS) {
				DP3T.addWorkerStartedToHistory(getApplicationContext(), "fake");
				boolean success = executeFakeRequest(getApplicationContext());
				if (success) {
					Logger.d(TAG, "finished with success");
				} else {
					Logger.e(TAG, "failed");
					return Result.retry();
				}
			} else {
				Logger.d(TAG, "outdated request is dropped.");
			}
			t_dummy += syncInterval();
			SecureStorage.getInstance(getApplicationContext()).setTDummy(t_dummy);
		}

		startFakeWorker(getApplicationContext(), ExistingWorkPolicy.APPEND, t_dummy);
		return Result.success();
	}


	private boolean executeFakeRequest(Context context) {
		try {
			AuthCodeRepository authCodeRepository = new AuthCodeRepository(context);
			AuthenticationCodeResponseModel accessTokenResponse =
					authCodeRepository.getAccessTokenSync(new AuthenticationCodeRequestModel(FAKE_AUTH_CODE, 1));
			String accessToken = accessTokenResponse.getAccessToken();

			CountDownLatch countdownLatch = new CountDownLatch(1);
			AtomicBoolean error = new AtomicBoolean(false);
			DP3T.sendFakeInfectedRequest(context, new ExposeeAuthMethodAuthorization(getAuthorizationHeader(accessToken)),
					() -> {
						countdownLatch.countDown();
					},
					() -> {
						error.set(true);
						countdownLatch.countDown();
					});
			countdownLatch.await();
			if (error.get()) return false;
			return true;
		} catch (IOException | ResponseError | InterruptedException e) {
			Logger.e(TAG, "fake request failed", e);
			return false;
		}
	}

	private String getAuthorizationHeader(String accessToken) {
		return "Bearer " + accessToken;
	}

	private static long syncInterval() {
		double newDelayDays = ExponentialDistribution.sampleFromStandard() / SAMPLING_RATE;
		return (long) (newDelayDays * FACTOR_DAY_MILLIS);
	}

}
