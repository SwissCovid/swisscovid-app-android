package ch.admin.bag.dp3t.travel;

import android.view.View;

import java.util.Objects;

public class ItemButton extends TravelRecyclerItem {

	int buttonTitleStringId;
	View.OnClickListener onClickListener;

	public ItemButton(int buttonTitleStringId, View.OnClickListener onClickListener) {
		this.buttonTitleStringId = buttonTitleStringId;
		this.onClickListener = onClickListener;
	}

	@Override
	ViewType getViewType() {
		return ViewType.BUTTON;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ItemButton that = (ItemButton) o;
		return buttonTitleStringId == that.buttonTitleStringId &&
				Objects.equals(onClickListener, that.onClickListener);
	}

}
