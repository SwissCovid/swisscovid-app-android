package ch.admin.bag.dp3t.checkin.diary.items;

public class ItemVenueVisitDayHeader extends VenueVisitRecyclerItem {

	private final String dayLabel;

	public ItemVenueVisitDayHeader(String dayLabel) {
		this.dayLabel = dayLabel;
	}

	public String getDayLabel() {
		return dayLabel;
	}

	@Override
	public ViewType getViewType() {
		return ViewType.DAY_HEADER;
	}

}
