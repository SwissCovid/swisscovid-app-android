package ch.admin.bag.dp3t.checkin.startup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper
import ch.admin.bag.dp3t.extensions.getAutoCheckoutDelay
import ch.admin.bag.dp3t.extensions.getCheckoutWarningDelay
import ch.admin.bag.dp3t.storage.SecureStorage

class BootCompletedReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		if (Intent.ACTION_BOOT_COMPLETED != intent.action) {
			return
		}
		invalidateCheckIn(context)
	}

	private fun invalidateCheckIn(context: Context) {
		val storage = SecureStorage.getInstance(context)
		val checkInState = storage.checkInState ?: return
		val checkedOut = CrowdNotifierReminderHelper.autoCheckoutIfNecessary(context, checkInState)
		if (!checkedOut) {
			val checkInTime = checkInState.checkInTime
			NotificationHelper.getInstance(context).startOngoingNotification(checkInTime, checkInState.venueInfo)
			CrowdNotifierReminderHelper.setCheckoutWarning(checkInTime, checkInState.venueInfo.getCheckoutWarningDelay(), context)
			CrowdNotifierReminderHelper.setAutoCheckOut(checkInTime, checkInState.venueInfo.getAutoCheckoutDelay(), context)
			CrowdNotifierReminderHelper.setReminder(checkInTime + checkInState.selectedReminderDelay, context)
		}
	}
}