package ch.admin.bag.dp3t.checkin.utils;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.crowdnotifier.android.sdk.CrowdNotifier;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.checkin.models.DiaryEntry;
import ch.admin.bag.dp3t.util.ReminderHelper;
import ch.admin.bag.dp3t.checkin.models.CheckInState;

public class CrowdNotifierReminderHelper extends BroadcastReceiver {

	public static final String ACTION_DID_AUTO_CHECKOUT = BuildConfig.APPLICATION_ID + ".ACTION_DID_AUTO_CHECKOUT";
	private static final int REMINDER_INTENT_ID = 12;
	private static final String ACTION_REMINDER = BuildConfig.APPLICATION_ID + ".ACTION_REMINDER";
	private static final int EIGHT_HOUR_REMINDER_INTENT_ID = 13;
	private static final String ACTION_EIGHT_HOUR_REMINDER = BuildConfig.APPLICATION_ID + ".ACTION_EIGHT_HOUR_REMINDER";
	private static final int AUTO_CHECK_OUT_INTENT_ID = 14;
	private static final String ACTION_AUTO_CHECKOUT = BuildConfig.APPLICATION_ID + ".ACTION_AUTO_CHECKOUT";
	private static final long EIGHT_HOURS = 1000L * 60 * 60 * 8;
	public static final long TWELVE_HOURS = 1000L * 60 * 60 * 12;

	public static void removeAllReminders(Context context) {
		removeReminder(context);
		remove8HourReminder(context);
		removeAutoCheckOut(context);
	}

	public static void removeReminder(Context context) {
		PendingIntent pendingIntent = getPendingIntent(context, false);
		removeReminder(pendingIntent, context);
	}

	public static void setReminder(long alarmTime, Context context) {
		PendingIntent pendingIntent = getPendingIntent(context, false);
		setReminder(alarmTime, pendingIntent, context);
	}

	public static void set8HourReminder(long checkInTime, Context context) {
		PendingIntent pendingIntent = getPendingIntent(context, true);
		setReminder(checkInTime + EIGHT_HOURS, pendingIntent, context);
	}

	public static void remove8HourReminder(Context context) {
		PendingIntent pendingIntent = getPendingIntent(context, true);
		removeReminder(pendingIntent, context);
	}

	public static void setAutoCheckOut(long checkInTime, Context context) {
		PendingIntent pendingIntent = getAutoCheckOutPendingIntent(context);
		setReminder(checkInTime + TWELVE_HOURS, pendingIntent, context);
	}

	public static void removeAutoCheckOut(Context context) {
		PendingIntent pendingIntent = getAutoCheckOutPendingIntent(context);
		removeReminder(pendingIntent, context);
	}

	private static void setReminder(long alarmTime, PendingIntent pendingIntent, Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (alarmTime <= System.currentTimeMillis()) {
			alarmManager.cancel(pendingIntent);
			return;
		}
		alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
	}

	private static void removeReminder(PendingIntent pendingIntent, Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	private static PendingIntent getPendingIntent(Context context, boolean eightHours) {
		Intent intent = new Intent(context, ReminderHelper.class);
		if (eightHours) {
			intent.setAction(ACTION_EIGHT_HOUR_REMINDER);
			return PendingIntent.getBroadcast(context, EIGHT_HOUR_REMINDER_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			intent.setAction(ACTION_REMINDER);
			return PendingIntent.getBroadcast(context, REMINDER_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
	}

	private static PendingIntent getAutoCheckOutPendingIntent(Context context) {
		Intent intent = new Intent(context, ReminderHelper.class);
		intent.setAction(ACTION_AUTO_CHECKOUT);
		return PendingIntent.getBroadcast(context, AUTO_CHECK_OUT_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		CheckInState checkInState = SecureStorage.getInstance(context).getCheckInState();
		if (ACTION_REMINDER.equals(intent.getAction()) ||
				ACTION_EIGHT_HOUR_REMINDER.equals(intent.getAction()) && checkInState != null) {
			NotificationHelper.getInstance(context).showReminderNotification();
		} else if (ACTION_AUTO_CHECKOUT.equals(intent.getAction())) {
			autoCheckoutIfNecessary(context, checkInState);
		}
	}

	public static boolean autoCheckoutIfNecessary(Context context, CheckInState checkInState) {
		if (checkInState == null || checkInState.getCheckInTime() > System.currentTimeMillis() - TWELVE_HOURS) {
			return false;
		}

		NotificationHelper notificationHelper = NotificationHelper.getInstance(context);
		notificationHelper.stopOngoingNotification();
		notificationHelper.removeReminderNotification();
		notificationHelper.showAutoCheckoutNotification();
		long checkIn = checkInState.getCheckInTime();
		long checkOut = checkIn + TWELVE_HOURS;
		long id = CrowdNotifier.addCheckIn(checkIn, checkOut, checkInState.getVenueInfo(), context);
		DiaryStorage.getInstance(context).addEntry(new DiaryEntry(id, checkIn, checkOut, checkInState.getVenueInfo(), ""));
		SecureStorage storage = SecureStorage.getInstance(context);
		storage.setCheckInState(null);
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_DID_AUTO_CHECKOUT));
		return true;
	}

}
