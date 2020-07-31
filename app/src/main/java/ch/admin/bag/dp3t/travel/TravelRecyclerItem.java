package ch.admin.bag.dp3t.travel;

public abstract class TravelRecyclerItem {
	enum ViewType {
		ICON_AND_TEXT, HEADER, BUTTON, COUNTRY
	}

	abstract ViewType getViewType();

}
