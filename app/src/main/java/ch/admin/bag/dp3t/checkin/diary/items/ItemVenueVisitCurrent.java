package ch.admin.bag.dp3t.checkin.diary.items;

import ch.admin.bag.dp3t.checkin.models.CheckInState;

public class ItemVenueVisitCurrent extends VenueVisitRecyclerItem {

	private final CheckInState checkInState;

	public ItemVenueVisitCurrent(CheckInState checkInState) {
		this.checkInState = checkInState;
	}

	public CheckInState getCheckInState() {
		return checkInState;
	}

	@Override
	public ViewType getViewType() {
		return ViewType.CURRENT;
	}

}
