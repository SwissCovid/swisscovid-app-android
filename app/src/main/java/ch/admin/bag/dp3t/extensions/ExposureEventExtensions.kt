package ch.admin.bag.dp3t.extensions

import android.content.Context
import ch.admin.bag.dp3t.util.StringUtil
import org.crowdnotifier.android.sdk.model.ExposureEvent

fun ExposureEvent.getDetailsString(context: Context): String {
	val dateString = StringUtil.getReportDateString(endTime, true, false, context)
	val startTimeString = StringUtil.getHourMinuteTimeString(startTime, ":")
	val endTimeString = StringUtil.getHourMinuteTimeString(endTime, ":")
	return "$dateString\n$startTimeString - $endTimeString"
}