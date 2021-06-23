package ch.admin.bag.dp3t.checkin.diary.items;

public abstract class VenueVisitRecyclerItem {

	public enum ViewType {
		VENUE, DAY_HEADER, CURRENT
	}

	public abstract ViewType getViewType();

}
