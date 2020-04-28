package org.dpppt.android.app.broadcastreceivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import org.dpppt.android.app.MainActivity;
import org.dpppt.android.app.R;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.app.util.NotificationUtil;

public class ConfigUpdateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SecureStorage secureStorage = SecureStorage.getInstance(context);
		if (secureStorage.getDoForceUpdate()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				NotificationUtil.createNotificationChannel(context);
			}

			Intent resultIntent = new Intent(context, MainActivity.class);
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent =
					PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
	}
}

