package ch.admin.bag.dp3t.travel;

import android.widget.CompoundButton;

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

}
