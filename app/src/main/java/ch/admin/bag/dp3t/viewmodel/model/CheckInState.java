package ch.admin.bag.dp3t.viewmodel.model;

import org.crowdnotifier.android.sdk.model.VenueInfo;

public class CheckInState {
	private boolean isCheckedIn;
	private VenueInfo venueInfo;
	private long checkInTime;
	private long checkOutTime;
	ReminderOption selectedTimerOption;

	public CheckInState(boolean isCheckedIn, VenueInfo venueInfo, long checkInTime, long checkOutTime,
			ReminderOption selectedTimerOption) {
		this.isCheckedIn = isCheckedIn;
		this.venueInfo = venueInfo;
		this.checkInTime = checkInTime;
		this.checkOutTime = checkOutTime;
		this.selectedTimerOption = selectedTimerOption;
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

	public ReminderOption getSelectedTimerOption() {
		return selectedTimerOption;
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

	public void setSelectedTimerOption(ReminderOption selectedTimerOption) {
		this.selectedTimerOption = selectedTimerOption;
	}

}
