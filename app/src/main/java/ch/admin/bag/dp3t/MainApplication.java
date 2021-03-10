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

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicInteger;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.logger.LogLevel;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.models.ApplicationInfo;
import org.dpppt.android.sdk.models.ExposureDay;
import org.dpppt.android.sdk.util.SignatureUtil;

import ch.admin.bag.dp3t.debug.DebugFragment;
import ch.admin.bag.dp3t.networking.CertificatePinning;
import ch.admin.bag.dp3t.networking.ConfigWorker;
import ch.admin.bag.dp3t.networking.FakeWorker;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.ActivityLifecycleCallbacksAdapter;
import ch.admin.bag.dp3t.util.NotificationUtil;

public class MainApplication extends Application {

	private static final long BACKGROUND_TIMEOUT_SESSION_MS = 30 * 60 * 1000L;

	@Override
	public void onCreate() {
		super.onCreate();

		if (DebugFragment.EXISTS) {
			Logger.init(getApplicationContext(), LogLevel.DEBUG);
			CertificatePinning.initDebug(this);
		}

		registerReceiver(contactUpdateReceiver, DP3T.getUpdateIntentFilter());

		initDP3T(this);

		SecureStorage secureStorage = SecureStorage.getInstance(this);
		int appVersionCode = BuildConfig.VERSION_CODE;
		if (secureStorage.getLastKnownAppVersionCode() != appVersionCode) {
			secureStorage.setLastKnownAppVersionCode(appVersionCode);
			// cancel any active fake workers (will be restarted below if needed)
			FakeWorker.stop(this);
		}

		FakeWorker.safeStartFakeWorker(this);
		ConfigWorker.scheduleConfigWorkerIfOutdated(this);

		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksAdapter() {
			private long tsActivitiesStop = 0;
			private AtomicInteger numCreated = new AtomicInteger(0);
			private AtomicInteger numStarted = new AtomicInteger(0);

			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				numCreated.incrementAndGet();
			}

			@Override
			public void onActivityStarted(Activity activity) {
				if (numStarted.getAndIncrement() == 0) {
					if (tsActivitiesStop > 0 && System.currentTimeMillis() - tsActivitiesStop > BACKGROUND_TIMEOUT_SESSION_MS) {
						Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
						mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(mainActivityIntent);
					}
					DP3T.addClientOpenedToHistory(MainApplication.this);
				}
			}

			@Override
			public void onActivityStopped(Activity activity) {
				if (numStarted.decrementAndGet() == 0) tsActivitiesStop = System.currentTimeMillis();
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				if (numCreated.decrementAndGet() == 0) tsActivitiesStop = 0;
			}
		});
	}

	public static void initDP3T(Context context) {
		PublicKey signaturePublicKey = SignatureUtil.getPublicKeyFromBase64OrThrow(BuildConfig.BUCKET_PUBLIC_KEY);
		DP3T.init(context, new ApplicationInfo(BuildConfig.REPORT_URL, BuildConfig.BUCKET_URL), signaturePublicKey,
				BuildConfig.DEV_HISTORY);

		DP3T.setCertificatePinner(CertificatePinning.getCertificatePinner());
		DP3T.setUserAgent(
				() -> context.getPackageName() + ";" + BuildConfig.VERSION_NAME + ";" + BuildConfig.BUILD_TIME + ";Android;" +
						Build.VERSION.SDK_INT + ";" + DP3T.getENModuleVersion(context));

		DP3T.setWithFederationGateway(context, true);
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

	private static void createNewContactNotification(Context context, int contactId) {
		SecureStorage secureStorage = SecureStorage.getInstance(context);

		NotificationUtil.generateContactNotification(context);

		secureStorage.setAppOpenAfterNotificationPending(true);
		secureStorage.setLeitfadenOpenPending(true);
		secureStorage.setReportsHeaderAnimationPending(true);
		secureStorage.setLastShownContactId(contactId);
	}

}
