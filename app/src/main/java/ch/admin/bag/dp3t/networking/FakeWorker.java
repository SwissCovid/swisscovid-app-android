/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.networking;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ch.admin.bag.dp3t.networking.errors.ResponseError;
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel;
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeResponseModel;
import ch.admin.bag.dp3t.util.ExponentialDistribution;
import ch.admin.bag.dp3t.util.JwtUtil;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.backend.models.ExposeeAuthMethodAuthorization;

public class FakeWorker extends Worker {

	private static final String TAG = "FakeWorker";
	private static final String WORK_TAG = "ch.admin.bag.dp3t.FakeWorker";
	private static final String FAKE_AUTH_CODE = "000000000000";

	private static final float SAMPLING_RATE = 0.2f;
	private static final long FACTOR_DAY_MILLIS = 24 * 60 * 60 * 60 * 1000L;

	public static void safeStartFakeWorker(Context context) {
		startFakeWorker(context, ExistingWorkPolicy.KEEP);
	}

	private static void startFakeWorker(Context context, ExistingWorkPolicy policy) {
		double newDelayDays = ExponentialDistribution.sampleFromStandard() / SAMPLING_RATE;
		long newDelayMillis = Math.round(FACTOR_DAY_MILLIS * newDelayDays);

		Constraints constraints = new Constraints.Builder()
				.setRequiredNetworkType(NetworkType.CONNECTED)
				.build();

		OneTimeWorkRequest fakeWorker = new OneTimeWorkRequest.Builder(FakeWorker.class)
				.setConstraints(constraints)
				.setInitialDelay(newDelayMillis, TimeUnit.MILLISECONDS)
				.build();
		WorkManager.getInstance(context).enqueueUniqueWork(WORK_TAG, policy, fakeWorker);
	}

	public FakeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		org.dpppt.android.sdk.internal.logger.Logger.d(TAG, "start FakeWorker");
		try {
			executeFakeRequest(getApplicationContext());
			startFakeWorker(getApplicationContext(), ExistingWorkPolicy.APPEND);
		} catch (IOException | ResponseError | InvalidAuthResponseException | NoSuchAlgorithmException e) {
			org.dpppt.android.sdk.internal.logger.Logger.d(TAG, "FakeWorker finished with exception " + e.getMessage());
			return Result.retry();
		}
		org.dpppt.android.sdk.internal.logger.Logger.d(TAG, "FakeWorker finished with success");
		return Result.success();
	}

	private void executeFakeRequest(Context context)
			throws IOException, ResponseError, InvalidAuthResponseException, NoSuchAlgorithmException {
		AuthCodeRepository authCodeRepository = new AuthCodeRepository(context);
		AuthenticationCodeResponseModel accessTokenResponse =
				authCodeRepository.getAccessTokenSync(new AuthenticationCodeRequestModel(FAKE_AUTH_CODE, 1));
		String accessToken = accessTokenResponse.getAccessToken();

		Date onsetDate = JwtUtil.getOnsetDate(accessToken);
		if (onsetDate == null) throw new InvalidAuthResponseException();

		DP3T.sendFakeInfectedRequest(context, onsetDate, new ExposeeAuthMethodAuthorization(accessToken));
	}

	private class InvalidAuthResponseException extends Throwable { }

}
