package ch.admin.bag.dp3t.checkin.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.MainActivity;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.StringUtil;

import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH;
import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW;

public class NotificationHelper {

	public static final String ACTION_CROWDNOTIFIER_REMINDER_NOTIFICATION = BuildConfig.APPLICATION_ID +
			".ACTION_CROWDNOTIFIER_REMINDER_NOTIFICATION";
	public static final String ACTION_AUTO_CHECKOUT_NOTIFICATION = BuildConfig.APPLICATION_ID +
			".ACTION_AUTO_CHECKOUT_NOTIFICATION";
	public static final String ACTION_ONGOING_NOTIFICATION = BuildConfig.APPLICATION_ID + ".ACTION_ONGOING_NOTIFICATION";
	public static final String ACTION_CHECK_OUT_NOW = BuildConfig.APPLICATION_ID + ".ACTION_CHECK_OUT_NOW";
	public static final String ACTION_SNOOZE = BuildConfig.APPLICATION_ID + ".ACTION_SNOOZE";

	private final String CHANNEL_ID_REMINDER = "Reminders";
	private final String CHANNEL_ID_ONGOING_CHECK_IN = "Ongoing Check In";

	private final int ONGOING_NOTIFICATION_ID = -1;
	private final int REMINDER_NOTIFICATION_ID = -2;
	private final int AUTO_CHECKOUT_NOTIFICATION_ID = -3;


	private static NotificationHelper instance;

	private Context context;
	private NotificationManager notificationManager;

	private NotificationHelper(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public static synchronized NotificationHelper getInstance(Context context) {
		if (instance == null) {
			instance = new NotificationHelper(context);
		}
		return instance;
	}

	private void createNotificationChannel(String channelId, String channelName, boolean silent, int importance) {
		if (Build.VERSION.SDK_INT >= 26) {
			NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
			if (silent) {
				channel.setSound(null, null);
				channel.enableVibration(false);
			}
			notificationManager.createNotificationChannel(channel);
		}
	}

	private PendingIntent createBasicPendingIntent(String notificationAction) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(notificationAction);
		return TaskStackBuilder.create(context)
				.addNextIntentWithParentStack(intent)
				.getPendingIntent(notificationAction.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private NotificationCompat.Builder getNotificationBuilder(String channelId) {
		return new NotificationCompat.Builder(context, channelId)
				.setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_notification)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setDefaults(Notification.DEFAULT_ALL);
	}

	public void showAutoCheckoutNotification() {
		createNotificationChannel(CHANNEL_ID_REMINDER, context.getString(R.string.android_reminder_channel_name), false,
				IMPORTANCE_HIGH);

		Notification notification = getNotificationBuilder(CHANNEL_ID_REMINDER)
				.setContentIntent(createBasicPendingIntent(ACTION_AUTO_CHECKOUT_NOTIFICATION))
				.setContentTitle(context.getString(R.string.auto_checkout_title))
				.setContentText(context.getString(R.string.auto_checkout_body))
				.build();

		notificationManager.notify(AUTO_CHECKOUT_NOTIFICATION_ID, notification);
	}

	public void showReminderNotification() {

		createNotificationChannel(CHANNEL_ID_REMINDER, context.getString(R.string.android_reminder_channel_name), false,
				IMPORTANCE_HIGH);

		Intent snoozeIntent = new Intent(context, NotificationQuickActionReceiver.class);
		snoozeIntent.setAction(ACTION_SNOOZE);
		PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 1, snoozeIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = getNotificationBuilder(CHANNEL_ID_REMINDER)
				.setContentIntent(createBasicPendingIntent(ACTION_CROWDNOTIFIER_REMINDER_NOTIFICATION))
				.setContentTitle(context.getString(R.string.checkout_reminder_title))
				.setContentText(context.getString(R.string.checkout_reminder_text))
				.addAction(R.drawable.ic_close,
						context.getString(R.string.ongoing_notification_checkout_quick_action),
						createBasicPendingIntent(ACTION_CHECK_OUT_NOW))
				.addAction(R.drawable.ic_snooze,
						context.getString(R.string.reminder_notification_snooze_action),
						snoozePendingIntent)
				.build();

		notificationManager.notify(REMINDER_NOTIFICATION_ID, notification);
	}

	public void removeReminderNotification() {
		notificationManager.cancel(REMINDER_NOTIFICATION_ID);
	}

	public void startOngoingNotification(long startTime, VenueInfo venueInfo) {
		createNotificationChannel(CHANNEL_ID_ONGOING_CHECK_IN,
				context.getString(R.string.android_ongoing_checkin_notification_channel_name), true, IMPORTANCE_LOW);
		Notification ongoingNotification = new NotificationCompat.Builder(context, CHANNEL_ID_ONGOING_CHECK_IN)
				.setSmallIcon(R.drawable.ic_qr_small)
				.setContentTitle(context.getString(R.string.ongoing_notification_title)
						.replace("{TIME}", StringUtil.getHourMinuteTimeString(startTime, ":")))
				.setContentText(venueInfo.getTitle())
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOngoing(true)
				.addAction(R.drawable.ic_close,
						context.getString(R.string.ongoing_notification_checkout_quick_action),
						createBasicPendingIntent(ACTION_CHECK_OUT_NOW))
				.setContentIntent(createBasicPendingIntent(ACTION_ONGOING_NOTIFICATION))
				.build();
		notificationManager.notify(ONGOING_NOTIFICATION_ID, ongoingNotification);
	}

	public void stopOngoingNotification() {
		notificationManager.cancel(ONGOING_NOTIFICATION_ID);
	}

}
