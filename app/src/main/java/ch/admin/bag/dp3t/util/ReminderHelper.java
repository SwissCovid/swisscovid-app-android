package ch.admin.bag.dp3t.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderHelper extends BroadcastReceiver {

	private static final int REMINDER_INTENT_ID = 14;
	private static final String KEY_REACTIVATE_TRACING_REMINDER_INTENT = "KEY_REACTIVATE_TRACING_REMINDER_INTENT";

	public static void removeTracingActivationReminder(Context context) {
		PendingIntent pendingIntent = getPendingIntent(context);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	public static void setTracingActivationReminder(long alarmTime, Context context) {
		PendingIntent pendingIntent = getPendingIntent(context);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (alarmTime <= System.currentTimeMillis()) {
			alarmManager.cancel(pendingIntent);
			return;
		}
		alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
	}

	private static PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, ReminderHelper.class);
		intent.putExtra(KEY_REACTIVATE_TRACING_REMINDER_INTENT, KEY_REACTIVATE_TRACING_REMINDER_INTENT);
		return PendingIntent.getBroadcast(context, REMINDER_INTENT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.hasExtra(KEY_REACTIVATE_TRACING_REMINDER_INTENT)) {
			NotificationUtil.showReminderNotification(context);
		}
	}

}
