/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.internal.backend.BackendBucketRepository;
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
		}
		if (!BuildConfig.DEBUG) {
			Fabric.with(this, new Crashlytics());
		}
	}

}
