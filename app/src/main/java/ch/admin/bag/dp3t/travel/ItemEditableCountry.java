package ch.admin.bag.dp3t.travel;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class ItemEditableCountry extends TravelRecyclerItem {

	String countryName;
	int flagResId;
	int addRemoveIconResId;
	boolean showDragAndDropIcon;
	View.OnClickListener onAddRemoveClickedListener;
	StartDragListener startDragListener;


	public ItemEditableCountry(String countryName, int flagResId, int addRemoveIconResId, boolean showDragAndDropIcon,
			View.OnClickListener onAddRemoveClickedListener, StartDragListener startDragListener) {
		this.countryName = countryName;
		this.flagResId = flagResId;
		this.addRemoveIconResId = addRemoveIconResId;
		this.showDragAndDropIcon = showDragAndDropIcon;
		this.onAddRemoveClickedListener = onAddRemoveClickedListener;
		this.startDragListener = startDragListener;
	}

	@Override
	ViewType getViewType() {
		return ViewType.EDITABLE_COUNTRY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemEditableCountry that = (ItemEditableCountry) o;
		return flagResId == that.flagResId &&
				addRemoveIconResId == that.addRemoveIconResId &&
				showDragAndDropIcon == that.showDragAndDropIcon &&
				Objects.equals(countryName, that.countryName)
				;
	}

	@Override
	public int hashCode() {
		return Objects.hash(countryName, flagResId);
	}

	interface StartDragListener {
		void startDrag(RecyclerView.ViewHolder viewHolder);

	}

}
