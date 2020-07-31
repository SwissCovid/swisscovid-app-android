package ch.admin.bag.dp3t.travel;

import android.widget.CompoundButton;

import java.util.Objects;

public class ItemCountry extends TravelRecyclerItem {

	String countryName;
	int flagResId;
	boolean isActive;
	boolean showTopSeparator;
	String statusText;
	CompoundButton.OnCheckedChangeListener checkedChangeListener;

	public ItemCountry(String countryName, int flagResId, boolean isActive, boolean showTopSeparator, String statusText,
			CompoundButton.OnCheckedChangeListener checkedChangeListener) {
		this.countryName = countryName;
		this.flagResId = flagResId;
		this.isActive = isActive;
		this.showTopSeparator = showTopSeparator;
		this.statusText = statusText;
		this.checkedChangeListener = checkedChangeListener;
	}

	@Override
	ViewType getViewType() {
		return ViewType.COUNTRY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemCountry that = (ItemCountry) o;
		return flagResId == that.flagResId &&
				isActive == that.isActive &&
				showTopSeparator == that.showTopSeparator &&
				Objects.equals(countryName, that.countryName) &&
				Objects.equals(statusText, that.statusText) &&
				Objects.equals(checkedChangeListener, that.checkedChangeListener);
	}

	@Override
	public int hashCode() {
		return Objects.hash(countryName, flagResId);
	}

}
