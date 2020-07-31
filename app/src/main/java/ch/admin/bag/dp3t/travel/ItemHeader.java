package ch.admin.bag.dp3t.travel;

import java.util.Objects;

public class ItemHeader extends TravelRecyclerItem {

	int headerTextResId;

	public ItemHeader(int headerTextResId) {
		this.headerTextResId = headerTextResId;
	}

	@Override
	ViewType getViewType() {
		return ViewType.HEADER;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemHeader that = (ItemHeader) o;
		return headerTextResId == that.headerTextResId;
	}

}
