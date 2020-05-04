/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.networking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.backend.SignatureException;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.errors.ResponseError;
import ch.admin.bag.dp3t.networking.models.ConfigResponseModel;
import ch.admin.bag.dp3t.networking.models.InfoBoxModel;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.NotificationUtil;

public class ConfigWorker extends Worker {

	private static final int REPEAT_INTERVAL_CONFIG_HOURS = 6;
	private static final String APP_VERSION_PREFIX_ANDROID = "android-";
	private static final String OS_VERSION_PREFIX_ANDROID = "android";

	private static final String TAG = "ConfigWorker";
	private static final String WORK_TAG = "ch.admin.bag.dp3t.ConfigWorker";

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
		org.dpppt.android.sdk.internal.logger.Logger.d(TAG, "start ConfigWorker");
		try {
			loadConfig();
		} catch (IOException | ResponseError | SignatureException e) {
			org.dpppt.android.sdk.internal.logger.Logger.d(TAG, "ConfigWorker finished with exception " + e.getMessage());
			return Result.retry();
		}

		org.dpppt.android.sdk.internal.logger.Logger.d(TAG, "ConfigWorker finished with success");
		return Result.success();
	}

	public void loadConfig() throws IOException, ResponseError, SignatureException {
		Context context = getApplicationContext();

		ConfigRepository configRepository = new ConfigRepository(context);

		String appVersion = APP_VERSION_PREFIX_ANDROID + BuildConfig.VERSION_NAME;
		String osVersion = OS_VERSION_PREFIX_ANDROID + Build.VERSION.SDK_INT;
		String buildNumber = String.valueOf(BuildConfig.BUILD_TIME);

		ConfigResponseModel config = configRepository.getConfig(appVersion, osVersion, buildNumber);

		SecureStorage secureStorage = SecureStorage.getInstance(context);
		secureStorage.setDoForceUpdate(config.getDoForceUpdate());

		if (config.getInfoBox() != null) {
			InfoBoxModel info = config.getInfoBox(context.getString(R.string.language_key));
			secureStorage.setInfoboxTitle(info.getTitle());
			secureStorage.setInfoboxText(info.getMsg());
			secureStorage.setInfoboxLinkTitle(info.getUrlTitle());
			secureStorage.setInfoboxLinkUrl(info.getUrl());
			secureStorage.setHasInfobox(true);
		} else {
			secureStorage.setHasInfobox(false);
		}

		boolean forceUpdate = secureStorage.getDoForceUpdate();
		if (forceUpdate) {
			if (!secureStorage.getForceUpdateLiveData().hasObservers()) {
				showNotification(context);
			}
		} else {
			cancelNotification(context);
		}

		boolean forceTraceShutdown = config.getForceTraceShutdown();
		if (forceTraceShutdown) {
			if (DP3T.isStarted(context)) {
				secureStorage.setForcedTraceShutdown(true);
				DP3T.stop(context);
			}
		} else {
			if (secureStorage.getForcedTraceShutdown() && !DP3T.isStarted(context)) {
				DP3T.start(context);
			}
			secureStorage.setForcedTraceShutdown(false);
		}

		DP3T.setMatchingParameters(context, config.getSdkConfig().getBadAttenuationThreshold(),
				config.getSdkConfig().getContactAttenuationThreshold(), config.getSdkConfig().getEventThreshold(),
				config.getSdkConfig().getNumberOfWindowsForExposure());
	}

	private void showNotification(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationUtil.createNotificationChannel(context);
		}

		String packageName = context.getPackageName();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=" + packageName));
		PendingIntent pendingIntent =
				PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification =
				new NotificationCompat.Builder(context, NotificationUtil.NOTIFICATION_CHANNEL_ID)
						.setContentTitle(context.getString(R.string.force_update_title))
						.setContentText(context.getString(R.string.force_update_text))
						.setPriority(NotificationCompat.PRIORITY_MAX)
						.setSmallIcon(R.drawable.ic_begegnungen)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true)
						.build();

		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NotificationUtil.NOTIFICATION_ID_UPDATE, notification);
	}

	private void cancelNotification(Context context) {
		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NotificationUtil.NOTIFICATION_ID_UPDATE);
	}

}
