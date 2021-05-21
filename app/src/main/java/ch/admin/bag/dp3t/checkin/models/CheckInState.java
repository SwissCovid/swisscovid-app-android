package ch.admin.bag.dp3t.checkin.models;

import org.crowdnotifier.android.sdk.model.VenueInfo;

public class CheckInState {
	private boolean isCheckedIn;
	private VenueInfo venueInfo;
	private long checkInTime;
	private long checkOutTime;
	private long selectedReminderDelay;

	public CheckInState(boolean isCheckedIn, VenueInfo venueInfo, long checkInTime, long checkOutTime,
			long selectedReminderDelay) {
		this.isCheckedIn = isCheckedIn;
		this.venueInfo = venueInfo;
		this.checkInTime = checkInTime;
		this.checkOutTime = checkOutTime;
		this.selectedReminderDelay = selectedReminderDelay;
	}

	public boolean isCheckedIn() {
		return isCheckedIn;
	}

	public VenueInfo getVenueInfo() {
		return venueInfo;
	}

	public long getCheckInTime() {
		return checkInTime;
	}

	public long getCheckOutTime() {
		return checkOutTime;
	}

	public long getSelectedReminderDelay() {
		return selectedReminderDelay;
	}

	public void setCheckedIn(boolean checkedIn) {
		isCheckedIn = checkedIn;
	}

	public void setCheckInTime(long checkInTime) {
		this.checkInTime = checkInTime;
	}

	public void setCheckOutTime(long checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public void setSelectedReminderDelay(long selectedReminderDelay) {
		this.selectedReminderDelay = selectedReminderDelay;
	}

}
