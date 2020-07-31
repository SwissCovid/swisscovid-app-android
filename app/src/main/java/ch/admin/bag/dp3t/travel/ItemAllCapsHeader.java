package ch.admin.bag.dp3t.travel;

import java.util.Objects;

public class ItemAllCapsHeader extends TravelRecyclerItem {

	int headerTextResId;

	public ItemAllCapsHeader(int headerTextResId) {
		this.headerTextResId = headerTextResId;
	}

	@Override
	ViewType getViewType() {
		return ViewType.ALL_CAPS_HEADER;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemAllCapsHeader that = (ItemAllCapsHeader) o;
		return headerTextResId == that.headerTextResId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(headerTextResId);
	}

}
