package ch.admin.bag.dp3t.checkin.models

import ch.admin.bag.dp3t.checkin.utils.CheckInRecord
import org.crowdnotifier.android.sdk.model.VenueInfo

data class CheckInState(
	var isCheckedIn: Boolean,
	override val venueInfo: VenueInfo,
	override var checkInTime: Long,
	override var checkOutTime: Long,
	var selectedReminderDelay: Long,
) : CheckInRecord {
	override val id: Long? = null // must be null
}
