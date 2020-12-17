/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.storage.SecureStorage;

public class NotificationRepeatWorker extends Worker {

	public static final String WORK_TAG = "ch.admin.bag.dp3t.util.NotificationRepeatWorker";

	private static final long NOTIFICATION_DELAY = BuildConfig.FLAVOR.equals("dev") ? 15 * 60 * 1000L : 4 * 60 * 60 * 1000L;

	public static void startWorker(Context context) {
		OneTimeWorkRequest notificationWorker = new OneTimeWorkRequest.Builder(NotificationRepeatWorker.class)
				.setInitialDelay(NOTIFICATION_DELAY, TimeUnit.MILLISECONDS)
				.addTag(WORK_TAG)
				.build();

		WorkManager.getInstance(context).enqueueUniqueWork(WORK_TAG, ExistingWorkPolicy.REPLACE, notificationWorker);
	}

	public NotificationRepeatWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		Context context = getApplicationContext();
		SecureStorage secureStorage = SecureStorage.getInstance(context);
		if (secureStorage.getAppOpenAfterNotificationPending()) {
			NotificationUtil.generateContactNotification(context);
		}
		return Result.success();
	}

}
