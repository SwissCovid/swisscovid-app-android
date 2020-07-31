package ch.admin.bag.dp3t.travel;

public class ItemHeader extends TravelRecyclerItem {

	int headerTextResId;

	public ItemHeader(int headerTextResId) {
		this.headerTextResId = headerTextResId;
	}

	@Override
	ViewType getViewType() {
		return ViewType.HEADER;
	}

}
