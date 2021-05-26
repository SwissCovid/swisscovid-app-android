package ch.admin.bag.dp3t.checkin.models;

import androidx.annotation.Keep;

import org.crowdnotifier.android.sdk.model.VenueInfo;

@Keep
public class DiaryEntry {
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

	public long getId() {
		return id;
	}

	public long getArrivalTime() {
		return arrivalTime;
	}

	public long getDepartureTime() {
		return departureTime;
	}

	public VenueInfo getVenueInfo() {
		return venueInfo;
	}

	public void setArrivalTime(long arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public void setDepartureTime(long departureTime) {
		this.departureTime = departureTime;
	}

}
