package ch.admin.bag.dp3t.travel;

public class ItemIconAndText extends TravelRecyclerItem {

	int textResourceId;
	int iconResourceId;
	int iconColorId;
	int backgroundColorId;

	public ItemIconAndText(int textResourceId, int iconResourceId, int iconColorId, int backgroundColorId) {
		this.textResourceId = textResourceId;
		this.iconResourceId = iconResourceId;
		this.iconColorId = iconColorId;
		this.backgroundColorId = backgroundColorId;
	}

	@Override
	ViewType getViewType() {
		return ViewType.ICON_AND_TEXT;
	}

}
