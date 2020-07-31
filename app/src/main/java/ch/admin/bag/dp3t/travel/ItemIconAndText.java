package ch.admin.bag.dp3t.travel;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemIconAndText that = (ItemIconAndText) o;
		return textResourceId == that.textResourceId &&
				iconResourceId == that.iconResourceId &&
				iconColorId == that.iconColorId &&
				backgroundColorId == that.backgroundColorId;
	}

}
