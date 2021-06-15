package ch.admin.bag.dp3t.checkin.models

import org.crowdnotifier.android.sdk.model.VenueInfo

interface CheckinInfo {
	val venueInfo: VenueInfo
	var checkInTime: Long
	var checkOutTime: Long
	val id: Long?
}