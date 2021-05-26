package ch.admin.bag.dp3t.checkin.models

import org.crowdnotifier.android.sdk.model.VenueInfo

data class CheckInState(
	var isCheckedIn: Boolean,
	val venueInfo: VenueInfo,
	var checkInTime: Long,
	var checkOutTime: Long,
	var selectedReminderDelay: Long
)
