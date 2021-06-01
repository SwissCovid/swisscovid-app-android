package ch.admin.bag.dp3t.extensions

import androidx.annotation.StringRes
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

@StringRes
fun VenueInfo.getSubtitle(): Int {
	return getSwissCovidLocationData().type.getNameRes()
}
