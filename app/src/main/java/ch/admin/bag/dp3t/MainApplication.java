/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import java.security.PublicKey;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.backend.BackendBucketRepository;
import org.dpppt.android.sdk.internal.logger.LogLevel;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.models.ApplicationInfo;
import org.dpppt.android.sdk.models.ExposureDay;
import org.dpppt.android.sdk.util.SignatureUtil;

import ch.admin.bag.dp3t.networking.CertificatePinning;
import ch.admin.bag.dp3t.networking.FakeWorker;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.NotificationUtil;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.IS_DEV) {
			BackendBucketRepository.BATCH_LENGTH = 5 * 60 * 1000L;
			Logger.init(getApplicationContext(), LogLevel.DEBUG);
		} else {
			Logger.init(getApplicationContext(), LogLevel.DEBUG);
		}

		registerReceiver(contactUpdateReceiver, DP3T.getUpdateIntentFilter());

		PublicKey signaturePublicKey = SignatureUtil.getPublicKeyFromBase64OrThrow(BuildConfig.BUCKET_PUBLIC_KEY);
		DP3T.init(this, new ApplicationInfo("dp3t-app", BuildConfig.REPORT_URL, BuildConfig.BUCKET_URL), signaturePublicKey);

		DP3T.setCertificatePinner(CertificatePinning.getCertificatePinner());
		DP3T.setUserAgent(getPackageName() + ";" + BuildConfig.VERSION_NAME + ";" + BuildConfig.BUILD_TIME + ";Android;" +
				Build.VERSION.SDK_INT);

		FakeWorker.safeStartFakeWorker(this);
	}

	private BroadcastReceiver contactUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			SecureStorage secureStorage = SecureStorage.getInstance(context);
			TracingStatus status = DP3T.getStatus(context);
			if (status.getInfectionStatus() == InfectionStatus.EXPOSED) {
				ExposureDay exposureDay = null;
				long dateNewest = 0;
				for (ExposureDay day : status.getExposureDays()) {
					if (day.getExposedDate().getStartOfDayTimestamp() > dateNewest) {
						exposureDay = day;
						dateNewest = day.getExposedDate().getStartOfDayTimestamp();
					}
				}
				if (exposureDay != null && secureStorage.getLastShownContactId() != exposureDay.getId()) {
					createNewContactNotification(context, exposureDay.getId());
				}
			}
		}
	};

	private void createNewContactNotification(Context context, int contactId) {
		SecureStorage secureStorage = SecureStorage.getInstance(context);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationUtil.createNotificationChannel(context);
		}

		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		resultIntent.setAction(MainActivity.ACTION_GOTO_REPORTS);

		PendingIntent pendingIntent =
				PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification =
				new NotificationCompat.Builder(context, NotificationUtil.NOTIFICATION_CHANNEL_ID)
						.setContentTitle(context.getString(R.string.push_exposed_title))
						.setContentText(context.getString(R.string.push_exposed_text))
						.setPriority(NotificationCompat.PRIORITY_MAX)
						.setSmallIcon(R.drawable.ic_begegnungen)
						.setContentIntent(pendingIntent)
						.setAutoCancel(true)
						.build();

		NotificationManager notificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NotificationUtil.NOTIFICATION_ID_CONTACT, notification);

		secureStorage.setHotlineCallPending(true);
		secureStorage.setReportsHeaderAnimationPending(true);
		secureStorage.setLastShownContactId(contactId);
	}

}
