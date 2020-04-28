/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.crashlytics.android.Crashlytics;

import org.dpppt.android.app.networking.ConfigWorker;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.internal.backend.BackendBucketRepository;
import org.dpppt.android.sdk.internal.database.models.MatchedContact;
import org.dpppt.android.sdk.internal.util.ProcessUtil;

import io.fabric.sdk.android.Fabric;


public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		if (BuildConfig.IS_DEV) {
			BackendBucketRepository.BATCH_LENGTH = 5 * 60 * 1000L;
		}
		if (ProcessUtil.isMainProcess(this)) {
			DP3T.init(this, new ApplicationInfo("dp3t-app", BuildConfig.REPORT_URL, BuildConfig.BUCKET_URL));
			ConfigWorker.startConfigWorker(this);
		}
		if (!BuildConfig.DEBUG) {
			Fabric.with(this, new Crashlytics());
		}
	}

}
