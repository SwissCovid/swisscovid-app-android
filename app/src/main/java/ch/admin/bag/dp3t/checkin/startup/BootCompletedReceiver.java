package ch.admin.bag.dp3t.checkin.startup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper;
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper;
import ch.admin.bag.dp3t.checkin.models.CheckInState;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			return;
		}

		invalidateCheckIn(context);
	}

	private void invalidateCheckIn(Context context) {
		SecureStorage storage = SecureStorage.getInstance(context);
		CheckInState checkInState = storage.getCheckInState();
		if (checkInState == null) {
			return;
		}

		boolean checkedOut = CrowdNotifierReminderHelper.autoCheckoutIfNecessary(context, checkInState);

		if (!checkedOut) {
			long checkInTime = checkInState.getCheckInTime();
			NotificationHelper.getInstance(context).startOngoingNotification(checkInTime, checkInState.getVenueInfo());
			CrowdNotifierReminderHelper.set8HourReminder(checkInTime, context);
			CrowdNotifierReminderHelper.setAutoCheckOut(checkInTime, context);
			CrowdNotifierReminderHelper.setReminder(checkInTime + checkInState.getSelectedTimerOption().getDelayMillis(), context);
		}
	}

}
