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
import java.util.concurrent.TimeUnit;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.models.ExposeeAuthMethodAuthorization;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.networking.errors.ResponseError;
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel;
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeResponseModel;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.PoissonDistribution;

public class FakeWorker extends Worker {

	private static final String TAG = "FakeWorker";
	private static final String WORK_TAG = "ch.admin.bag.dp3t.FakeWorker";
	private static final String FAKE_AUTH_CODE = "000000000000";

	private static final long FACTOR_DAY_MILLIS = 24 * 60 * 60 * 1000L;
	private static final long FACTOR_HOUR_MILLIS = 60 * 60 * 1000L;
	private static final long MAX_DELAY_HOURS = 48;
	private static final double LAMBDA_HOURS = BuildConfig.FLAVOR.equals("dev") ? 24 : 120;
	private static final String KEY_T_DUMMY = "KEY_T_DUMMY";

	public static void safeStartFakeWorker(Context context) {
		long t_dummy = System.currentTimeMillis() + PoissonDistribution.sample(LAMBDA_HOURS) * FACTOR_HOUR_MILLIS;
		startFakeWorker(context, ExistingWorkPolicy.KEEP, t_dummy);
	}

	private static void startFakeWorker(Context context, ExistingWorkPolicy policy, long t_dummy) {

		long now = System.currentTimeMillis();
		long executionDelay = Math.max(0L, t_dummy - now - MAX_DELAY_HOURS * FACTOR_HOUR_MILLIS);
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
		if (t_dummy >= now - FACTOR_HOUR_MILLIS * MAX_DELAY_HOURS) {
			Logger.d(TAG, "start");
			DP3T.addWorkerStartedToHistory(getApplicationContext(), "fake");
			try {
				executeFakeRequest(getApplicationContext());
				Logger.d(TAG, "finished with success");
			} catch (IOException | ResponseError e) {
				Logger.e(TAG, "failed", e);
				return Result.retry();
			}
		} else {
			Logger.d(TAG, "outdated request is dropped.");
		}
		long new_t_dummy = PoissonDistribution.sample(LAMBDA_HOURS) * FACTOR_HOUR_MILLIS + t_dummy;
		startFakeWorker(getApplicationContext(), ExistingWorkPolicy.APPEND, new_t_dummy);
		return Result.success();
	}

	private void executeFakeRequest(Context context)
			throws IOException, ResponseError {
		AuthCodeRepository authCodeRepository = new AuthCodeRepository(context);
		AuthenticationCodeResponseModel accessTokenResponse =
				authCodeRepository.getAccessTokenSync(new AuthenticationCodeRequestModel(FAKE_AUTH_CODE, 1));
		String accessToken = accessTokenResponse.getAccessToken();

		DP3T.sendFakeInfectedRequest(context, new ExposeeAuthMethodAuthorization(getAuthorizationHeader(accessToken)));
	}

	private String getAuthorizationHeader(String accessToken) {
		return "Bearer " + accessToken;
	}

}
