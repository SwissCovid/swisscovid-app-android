package ch.admin.bag.dp3t.extensions

import androidx.annotation.StringRes
import ch.admin.bag.dp3t.checkin.models.QRCodePayload
import ch.admin.bag.dp3t.checkin.models.ReminderOption
import ch.admin.bag.dp3t.checkin.models.SwissCovidLocationData
import com.google.protobuf.InvalidProtocolBufferException
import org.crowdnotifier.android.sdk.model.VenueInfo

private const val ONE_MINUTE_IN_MILLIS = 60 * 1000L

fun VenueInfo.getSwissCovidLocationData(): SwissCovidLocationData {
	return if (countryData == null) {
		SwissCovidLocationData.newBuilder().build()
	} else {
		try {
			SwissCovidLocationData.parseFrom(countryData)
		} catch (e: InvalidProtocolBufferException) {
			SwissCovidLocationData.newBuilder().build()
		}
	}
}

@StringRes
fun VenueInfo.getSubtitle(): Int {
	return getSwissCovidLocationData().type.getNameRes()
}

fun VenueInfo.toQrCodePayload(): QRCodePayload {
	try {
		return QRCodePayload.parseFrom(qrCodePayload)
	} catch (e: InvalidProtocolBufferException) {
		throw RuntimeException("VenueInfo contains invalid qrCodePayload bytes!")
	}
}

fun VenueInfo.getAutoCheckoutDelay() = getSwissCovidLocationData().automaticCheckoutDelaylMs

fun VenueInfo.getCheckoutWarningDelay() = getSwissCovidLocationData().checkoutWarningDelayMs

fun VenueInfo.getReminderDelayOptions(): List<ReminderOption> {
	if (getSwissCovidLocationData().reminderDelayOptionsMsList.size == 0) {
		//Fallback if reminderDelayOptionsMsList is empty (30, 60, 120 and 240 minutes)
		return listOf(30, 60, 120, 240).map { ReminderOption(it * ONE_MINUTE_IN_MILLIS) }
	}
	return getSwissCovidLocationData().reminderDelayOptionsMsList.asSequence().distinct().filter { it > 0 }.sorted()
		.map { ReminderOption(it) }.take(4).toList()
}

