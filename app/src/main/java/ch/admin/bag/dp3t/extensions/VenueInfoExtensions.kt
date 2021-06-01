package ch.admin.bag.dp3t.extensions

import android.content.Context
import ch.admin.bag.dp3t.checkin.models.QRCodePayload
import ch.admin.bag.dp3t.checkin.models.ReminderOption
import com.google.protobuf.InvalidProtocolBufferException
import org.crowdnotifier.android.sdk.model.VenueInfo

private const val ONE_MINUTE_IN_MILLIS = 60 * 1000L

fun VenueInfo.toQrCodePayload(): QRCodePayload {
	try {
		return QRCodePayload.parseFrom(qrCodePayload)
	} catch (e: InvalidProtocolBufferException) {
		throw RuntimeException("VenueInfo contains invalid qrCodePayload bytes!")
	}
}

fun VenueInfo.getAutoCheckoutDelay() = getSwissCovidLocationData().automaticCheckoutDelaylMs

fun VenueInfo.getCheckoutWarningDelay() = getSwissCovidLocationData().checkoutWarningDelayMs

fun VenueInfo.getReminderDelayOptions(context: Context): List<ReminderOption> {
	val filteredResult = getSwissCovidLocationData().reminderDelayOptionsMsList.asSequence()
		.filter { it >= ONE_MINUTE_IN_MILLIS && it < getAutoCheckoutDelay() }
		.sorted()
		.map { ReminderOption(it) }
		.distinctBy { it.getDisplayString(context) }
		.take(4)
		.toList()
	return if (filteredResult.isEmpty()) {
		//Fallback if reminderDelayOptionsMsList is empty (30, 60, 120 and 240 minutes)
		listOf(30, 60, 120, 240).map { ReminderOption(it * ONE_MINUTE_IN_MILLIS) }
	} else {
		filteredResult
	}
}

