package ch.admin.bag.dp3t.checkin.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.checkin.utils.CheckInRecord;

@Keep
public class DiaryEntry implements CheckInRecord {

	private long id;
	private long arrivalTime;
	private long departureTime;
	private VenueInfo venueInfo;

	public DiaryEntry(long id, long arrivalTime, long departureTime, VenueInfo venueInfo) {
		this.id = id;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.venueInfo = venueInfo;
	}

	@Override
	@NonNull
	public Long getId() {
		return id;
	}

	@Override
	public long getCheckInTime() {
		return arrivalTime;
	}

	@Override
	public long getCheckOutTime() {
		return departureTime;
	}

	@Override
	public VenueInfo getVenueInfo() {
		return venueInfo;
	}

	@Override
	public void setCheckInTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	@Override
	public void setCheckOutTime(long departureTime) {
		this.departureTime = departureTime;
	}

}
