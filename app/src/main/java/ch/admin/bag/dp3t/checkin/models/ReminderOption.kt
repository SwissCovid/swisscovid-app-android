package ch.admin.bag.dp3t.checkin.models

import android.content.Context
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.util.StringUtil
import java.util.concurrent.TimeUnit

class ReminderOption(val delayMillis: Long) {

	fun getDisplayString(context: Context): String {
		return if (delayMillis < TimeUnit.MINUTES.toMillis(1)) {
			context.getString(R.string.reminder_option_off)
		} else {
			StringUtil.getShortDurationStringWithUnits(delayMillis, context)
		}
	}

}