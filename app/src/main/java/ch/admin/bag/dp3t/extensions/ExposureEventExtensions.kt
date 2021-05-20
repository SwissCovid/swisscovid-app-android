package ch.admin.bag.dp3t.extensions

import android.content.Context
import ch.admin.bag.dp3t.util.StringUtil
import org.crowdnotifier.android.sdk.model.ExposureEvent
import org.dpppt.android.sdk.models.DayDate

fun ExposureEvent.getDetailsString(context: Context): String {
	return "${StringUtil.getReportDateString(DayDate(endTime), true, false, context)}\n${
		StringUtil.getHourMinuteTimeString(startTime, ":")
	} - ${StringUtil.getHourMinuteTimeString(endTime, ":")}"

}