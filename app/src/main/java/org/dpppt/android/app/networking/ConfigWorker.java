/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.networking;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.work.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.dpppt.android.app.BuildConfig;
import org.dpppt.android.app.networking.errors.ResponseError;
import org.dpppt.android.app.networking.models.ConfigResponseModel;
import org.dpppt.android.app.networking.models.InfoBoxModel;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.sdk.DP3T;

public class ConfigWorker extends Worker {

	private static final int REPEAT_INTERVAL_CONFIG_HOURS = 6;
	private static final String APP_VERSION_PREFIX_ANDROID = "android-";
	private static final String OS_VERSION_PREFIX_ANDROID = "android";

	public static final String ACTION_CONFIG_UPDATE = "org.dpppt.android.app.ACTION_CONFIG_UPDATE";

	private static final String WORK_TAG = "org.dpppt.android.app.ConfigWorker";

	public static void startConfigWorker(Context context) {
		Constraints constraints = new Constraints.Builder()
				.setRequiredNetworkType(NetworkType.CONNECTED)
				.build();

		PeriodicWorkRequest periodicWorkRequest =
				new PeriodicWorkRequest.Builder(ConfigWorker.class, REPEAT_INTERVAL_CONFIG_HOURS, TimeUnit.HOURS)
						.setConstraints(constraints)
						.build();

		WorkManager workManager = WorkManager.getInstance(context);
		workManager.enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
	}

	public static void stopConfigWorker(Context context) {
		WorkManager workManager = WorkManager.getInstance(context);
		workManager.cancelAllWorkByTag(WORK_TAG);
	}

	public ConfigWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		Context context = getApplicationContext();
		try {
			loadConfig(context);
		} catch (IOException | ResponseError e) {
			return Result.retry();
		}

		return Result.success();
	}

	public static void loadConfig(Context context) throws IOException, ResponseError {

		ConfigRepository configRepository =	new ConfigRepository(context);

		String appVersion = APP_VERSION_PREFIX_ANDROID + BuildConfig.VERSION_NAME;
		String osVersion = OS_VERSION_PREFIX_ANDROID + Build.VERSION.SDK_INT;

		ConfigResponseModel config = configRepository.getConfig(appVersion, osVersion);

		SecureStorage secureStorage = SecureStorage.getInstance(context);
		secureStorage.setDoForceUpdate(config.getDoForceUpdate());
		if (config.getInfoBox() != null) {
			InfoBoxModel info = config.getInfoBox();
			secureStorage.setHasInfobox(true);
			secureStorage.setInfoboxTitle(info.getTitle());
			secureStorage.setInfoboxText(info.getMsg());
			secureStorage.setInfoboxLinkTitle(info.getUrlTitle());
			secureStorage.setInfoboxLinkUrl(info.getUrl());
		} else {
			secureStorage.setHasInfobox(false);
		}

		Intent intent = new Intent(ACTION_CONFIG_UPDATE);
		context.sendBroadcast(intent);
	}

}
