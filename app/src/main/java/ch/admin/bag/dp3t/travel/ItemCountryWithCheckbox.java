package ch.admin.bag.dp3t.travel;

import android.widget.CompoundButton;

import java.util.Objects;

public class ItemCountryWithCheckbox extends TravelRecyclerItem {

	String countryName;
	int flagResId;
	boolean isChecked;
	boolean showTopSeparator;
	CompoundButton.OnCheckedChangeListener checkedChangeListener;
	boolean showCheckBox;

	public ItemCountryWithCheckbox(String countryName, int flagResId, boolean isChecked, boolean showTopSeparator,
			CompoundButton.OnCheckedChangeListener checkedChangeListener, boolean showCheckBox) {
		this.countryName = countryName;
		this.flagResId = flagResId;
		this.isChecked = isChecked;
		this.showTopSeparator = showTopSeparator;
		this.checkedChangeListener = checkedChangeListener;
		this.showCheckBox = showCheckBox;
	}

	@Override
	ViewType getViewType() {
		return ViewType.COUNTRY_WITH_CHECKBOX;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemCountryWithCheckbox that = (ItemCountryWithCheckbox) o;
		return flagResId == that.flagResId &&
				isChecked == that.isChecked &&
				showTopSeparator == that.showTopSeparator &&
				showCheckBox == that.showCheckBox &&
				Objects.equals(countryName, that.countryName) &&
				Objects.equals(checkedChangeListener, that.checkedChangeListener);
	}

	@Override
	public int hashCode() {
		return Objects.hash(countryName, flagResId);
	}

}
