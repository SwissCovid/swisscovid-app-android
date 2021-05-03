package ch.admin.bag.dp3t.checkin.utils

import ch.admin.bag.dp3t.checkin.models.QRCodePayload
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.model.VenueInfo
import org.crowdnotifier.android.sdk.utils.Base64Util

fun QRCodePayload.toVenueInfo(): VenueInfo {
	return CrowdNotifier.getVenueInfo("?v=3#" + Base64Util.toBase64(this.toByteArray()), "")
}