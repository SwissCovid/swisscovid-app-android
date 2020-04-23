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
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.dpppt.android.app.util.DebugUtils;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.database.models.MatchedContact;
import org.dpppt.android.sdk.internal.util.ProcessUtil;


public class MainApplication extends Application {

	private static final String NOTIFICATION_CHANNEL_ID = "contact-channel";
	private static final String PREFS_NOTIFICATION = "PREFS_NOTIFICATION";
	private static final String PREF_LAST_ID_SHOWN = "PREF_LAST_CONTACT_SHOWN";
	private static final String PREF_ID_TO_SHOW = "PREF_ID_TO_SHOW";
	private static final int NOTIFICATION_ID = 42;

	@Override
	public void onCreate() {
		super.onCreate();
		if (ProcessUtil.isMainProcess(this)) {
			registerReceiver(broadcastReceiver, DP3T.getUpdateIntentFilter());
			DP3T.init(this, "org.dpppt.demo", DebugUtils.isDev());
		}
	}

	@Override
	public void onTerminate() {
		if (ProcessUtil.isMainProcess(this)) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onTerminate();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			SharedPreferences prefs = context.getSharedPreferences(PREFS_NOTIFICATION, Context.MODE_PRIVATE);
			TracingStatus status = DP3T.getStatus(context);
			if (status.getInfectionStatus() == InfectionStatus.EXPOSED) {
				MatchedContact newestContact = null;
				long dateNewest = 0;
				for (MatchedContact contact : status.getMatchedContacts()) {
					if (contact.getReportDate() > dateNewest) {
						newestContact = contact;
						dateNewest = contact.getReportDate();
					}
				}
				if (newestContact != null && prefs.getInt(PREF_LAST_ID_SHOWN, -1) != newestContact.getId()) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						createNotificationChannel();
					}

					Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());

					PendingIntent contentIntent = null;
					if (launchIntent != null) {
						contentIntent =
								PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					}
					Notification notification =
							new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
									.setContentTitle(context.getString(R.string.push_exposed_title))
									.setContentText(context.getString(R.string.push_exposed_text))
									.setPriority(NotificationCompat.PRIORITY_MAX)
									.setSmallIcon(R.drawable.ic_begegnungen)
									.setContentIntent(contentIntent)
									.setAutoCancel(true)
									.build();

					NotificationManager notificationManager =
							(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(NOTIFICATION_ID, notification);
					prefs.edit().putInt(PREF_ID_TO_SHOW, newestContact.getId()).commit();
				}
			}
		}
	};

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void createNotificationChannel() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelName = getString(R.string.app_name);
		NotificationChannel channel =
				new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		notificationManager.createNotificationChannel(channel);
	}

	public static Integer getAndClearContactToShowId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NOTIFICATION, Context.MODE_PRIVATE);
		if (prefs.contains(PREF_ID_TO_SHOW)) {
			int id = prefs.getInt(PREF_ID_TO_SHOW, 0);
			prefs.edit().remove(PREF_ID_TO_SHOW);
			return id;
		}
		return null;
	}

	public static void saveLaunchByContactId(Context context, int matchedContactId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NOTIFICATION, Context.MODE_PRIVATE);
		prefs.edit().putInt(PREF_LAST_ID_SHOWN, matchedContactId).commit();
	}

}
