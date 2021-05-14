package ch.admin.bag.dp3t.checkin.utils

import ch.admin.bag.dp3t.checkin.models.QRCodePayload
import org.crowdnotifier.android.sdk.model.VenueInfo
import org.crowdnotifier.android.sdk.utils.Base64Util
import org.crowdnotifier.android.sdk.utils.QrUtils

fun QRCodePayload.toVenueInfo(): VenueInfo {
	return QrUtils.getVenueInfoFromQrCode(Base64Util.toBase64(this.toByteArray()))
}