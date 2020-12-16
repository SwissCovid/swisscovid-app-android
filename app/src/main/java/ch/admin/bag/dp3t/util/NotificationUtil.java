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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import ch.admin.bag.dp3t.MainActivity;
import ch.admin.bag.dp3t.R;

public class NotificationUtil {

	public static final String ACTION_ACTIVATE_TRACING = "ACTION_ACTIVATE_TRACING";

	public static final String NOTIFICATION_CHANNEL_ID = "contact-channel";
	public static final String CHANNEL_ID_REMINDER = "CHANNEL_ID_REMINDER";
	public static final int NOTIFICATION_ID_CONTACT = 42;
	public static final int NOTIFICATION_ID_UPDATE = 43;
	public static final int NOTIFICATION_ID_REMINDER = 44;


	@RequiresApi(api = Build.VERSION_CODES.O)
	public static void createNotificationChannel(Context context) {
		String channelName = context.getString(R.string.app_name);
		createNotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, Notification.VISIBILITY_PRIVATE, context);
	}

	public static void showReminderNotification(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel(CHANNEL_ID_REMINDER, context.getString(R.string.android_reminder_channel_name),
					Notification.VISIBILITY_PUBLIC, context);
		}
		PendingIntent pendingIntent = createReminderPendingIntent(context);
		String title = context.getString(R.string.tracing_reminder_notification_title);
		String message = context.getString(R.string.tracing_reminder_notification_subtitle);

		Notification notification = createNotification(title, message, pendingIntent, CHANNEL_ID_REMINDER, context);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(message.hashCode(), notification);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private static void createNotificationChannel(String channelId, String channelName, int lockscreenVisibility,
			Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
			channel.setLockscreenVisibility(lockscreenVisibility);
			notificationManager.createNotificationChannel(channel);
		}
	}

	private static PendingIntent createReminderPendingIntent(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(ACTION_ACTIVATE_TRACING);
		return TaskStackBuilder.create(context)
				.addNextIntentWithParentStack(intent)
				.getPendingIntent(NOTIFICATION_ID_REMINDER, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static Notification createNotification(String title, String message, PendingIntent pendingIntent, String channelId,
			Context context) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(R.drawable.ic_begegnungen)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
		return builder.build();
	}

}
