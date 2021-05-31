package ch.admin.bag.dp3t.extensions

import androidx.annotation.StringRes
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.VenueType

@StringRes
fun VenueType.getNameRes(): Int {
	return when (this) {
		VenueType.USER_QR_CODE -> R.string.venue_type_user_qr_code
		VenueType.CONTACT_TRACING_QR_CODE -> R.string.venue_type_contact_tracing_qr_code
		else -> 0
	}
}