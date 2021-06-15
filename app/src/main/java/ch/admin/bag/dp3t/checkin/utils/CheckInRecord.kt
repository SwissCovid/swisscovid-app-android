package ch.admin.bag.dp3t.checkin.utils

import org.crowdnotifier.android.sdk.model.VenueInfo

interface CheckInRecord {
	val venueInfo: VenueInfo
	var checkInTime: Long
	var checkOutTime: Long
	val id: Long?
}