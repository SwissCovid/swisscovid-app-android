package ch.admin.bag.dp3t.extensions

import android.content.Context
import ch.admin.bag.dp3t.util.StringUtil
import org.crowdnotifier.android.sdk.model.ExposureEvent

fun ExposureEvent.getDetailsString(context: Context): String {
	return "${StringUtil.getReportDateString(endTime, true, false, context)}\n${
		StringUtil.getHourMinuteTimeString(startTime, ":")
	} - ${StringUtil.getHourMinuteTimeString(endTime, ":")}"

}