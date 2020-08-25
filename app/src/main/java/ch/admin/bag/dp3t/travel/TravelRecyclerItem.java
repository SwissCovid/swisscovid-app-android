package ch.admin.bag.dp3t.travel;

public abstract class TravelRecyclerItem {
	enum ViewType {
		ICON_AND_TEXT, HEADER, ALL_CAPS_HEADER, BUTTON, COUNTRY, EDITABLE_COUNTRY, SPACE, COUNTRY_WITH_CHECKBOX
	}

	abstract ViewType getViewType();

}
