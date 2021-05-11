package ch.admin.bag.dp3t.checkin.utils

import ch.admin.bag.dp3t.checkin.models.QRCodePayload
import ch.admin.bag.dp3t.checkin.models.ReminderOption
import ch.admin.bag.dp3t.checkin.models.SwissCovidLocationData
import com.google.protobuf.InvalidProtocolBufferException
import org.crowdnotifier.android.sdk.model.VenueInfo

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

fun VenueInfo.getSubtitle(): String {
	//TODO: This is to be defined what the subtitle will be for SwissCovid
	return "*subtitle*"
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

fun VenueInfo.getReminderDelayOptions() = getSwissCovidLocationData().reminderDelayOptionsMsList.map { ReminderOption(it) }


