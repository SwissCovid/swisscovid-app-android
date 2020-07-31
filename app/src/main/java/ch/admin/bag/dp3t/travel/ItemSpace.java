package ch.admin.bag.dp3t.travel;

import java.util.Objects;

public class ItemSpace extends TravelRecyclerItem {

	int height;
	int backgroundColorResId;

	public ItemSpace(int height, int backgroundColorResId) {
		this.height = height;
		this.backgroundColorResId = backgroundColorResId;
	}

	@Override
	ViewType getViewType() {
		return ViewType.SPACE;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemSpace itemSpace = (ItemSpace) o;
		return height == itemSpace.height &&
				backgroundColorResId == itemSpace.backgroundColorResId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(height, backgroundColorResId);
	}

}
