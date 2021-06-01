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
import ch.admin.bag.dp3t.extensions.VenueInfoExtensionsKt;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.checkin.models.DiaryEntry;
import ch.admin.bag.dp3t.checkin.models.CheckInState;

public class CrowdNotifierReminderHelper extends BroadcastReceiver {

	public static final String ACTION_DID_AUTO_CHECKOUT = BuildConfig.APPLICATION_ID + ".ACTION_DID_AUTO_CHECKOUT";
	private static final int REMINDER_INTENT_ID = 12;
	private static final String ACTION_REMINDER = BuildConfig.APPLICATION_ID + ".ACTION_REMINDER";
	private static final int CHECKOUT_WARNING_INTENT_ID = 13;
	private static final String ACTION_CHECKOUT_WARNING = BuildConfig.APPLICATION_ID + ".ACTION_CHECKOUT_WARNING";
	private static final int AUTO_CHECK_OUT_INTENT_ID = 14;
	private static final String ACTION_AUTO_CHECKOUT = BuildConfig.APPLICATION_ID + ".ACTION_AUTO_CHECKOUT";

	public static void removeAllReminders(Context context) {
		removeReminder(context);
		removeCheckoutWarning(context);
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

	public static void setCheckoutWarning(long checkInTime, long delay, Context context) {
		PendingIntent pendingIntent = getPendingIntent(context, true);
		setReminder(checkInTime + delay, pendingIntent, context);
	}

	public static void removeCheckoutWarning(Context context) {
		PendingIntent pendingIntent = getPendingIntent(context, true);
		removeReminder(pendingIntent, context);
	}

	public static void setAutoCheckOut(long checkInTime, long delay, Context context) {
		PendingIntent pendingIntent = getAutoCheckOutPendingIntent(context);
		setReminder(checkInTime + delay, pendingIntent, context);
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
		Intent intent = new Intent(context, CrowdNotifierReminderHelper.class);
		if (eightHours) {
			intent.setAction(ACTION_CHECKOUT_WARNING);
			return PendingIntent.getBroadcast(context, CHECKOUT_WARNING_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			intent.setAction(ACTION_REMINDER);
			return PendingIntent.getBroadcast(context, REMINDER_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
	}

	private static PendingIntent getAutoCheckOutPendingIntent(Context context) {
		Intent intent = new Intent(context, CrowdNotifierReminderHelper.class);
		intent.setAction(ACTION_AUTO_CHECKOUT);
		return PendingIntent.getBroadcast(context, AUTO_CHECK_OUT_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		CheckInState checkInState = SecureStorage.getInstance(context).getCheckInState();
		if (ACTION_REMINDER.equals(intent.getAction()) ||
				ACTION_CHECKOUT_WARNING.equals(intent.getAction()) && checkInState != null) {
			NotificationHelper.getInstance(context).showReminderNotification();
		} else if (ACTION_AUTO_CHECKOUT.equals(intent.getAction())) {
			autoCheckoutIfNecessary(context, checkInState);
		}
	}

	public static boolean autoCheckoutIfNecessary(Context context, CheckInState checkInState) {
		if (checkInState == null) {
			return false;
		}
		long autoCheckoutDelay = VenueInfoExtensionsKt.getAutoCheckoutDelay(checkInState.getVenueInfo());
		if (checkInState.getCheckInTime() > System.currentTimeMillis() - autoCheckoutDelay) {
			return false;
		}

		NotificationHelper notificationHelper = NotificationHelper.getInstance(context);
		notificationHelper.stopOngoingNotification();
		notificationHelper.removeReminderNotification();
		notificationHelper.showAutoCheckoutNotification();
		long checkIn = checkInState.getCheckInTime();
		long checkOut = checkIn + autoCheckoutDelay;
		long id = CrowdNotifier.addCheckIn(checkIn, checkOut, checkInState.getVenueInfo(), context);
		DiaryStorage.getInstance(context).addEntry(new DiaryEntry(id, checkIn, checkOut, checkInState.getVenueInfo()));
		SecureStorage storage = SecureStorage.getInstance(context);
		storage.setCheckInState(null);
		LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_DID_AUTO_CHECKOUT));
		return true;
	}

}
