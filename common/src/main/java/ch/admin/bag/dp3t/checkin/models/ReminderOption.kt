package ch.admin.bag.dp3t.checkin.models

import android.content.Context
import ch.admin.bag.dp3t.R
import java.util.concurrent.TimeUnit

class ReminderOption(val delayMillis: Long) {

	fun getDisplayString(context: Context): String {
		return when {
			delayMillis < TimeUnit.MINUTES.toMillis(1) -> context.getString(R.string.reminder_option_off)
			delayMillis < TimeUnit.HOURS.toMillis(1) -> {
				val minutes = TimeUnit.MILLISECONDS.toMinutes(delayMillis)
				minutes.toMinutesString(context)
			}
			else -> {
				val hours = TimeUnit.MILLISECONDS.toHours(delayMillis)
				val minutes = TimeUnit.MILLISECONDS.toMinutes(delayMillis) - TimeUnit.HOURS.toMinutes(hours)
				if (minutes > 0L) {
					"${hours.toHoursString(context)} ${minutes.toMinutesString(context)}"
				} else {
					hours.toHoursString(context)
				}
			}
		}
	}

	private fun Long.toHoursString(context: Context): String {
		return context.getString(R.string.reminder_option_hours).replace("{HOURS}", this.toString())
	}

	private fun Long.toMinutesString(context: Context): String {
		return context.getString(R.string.reminder_option_minutes).replace("{MINUTES}", this.toString())
	}

}