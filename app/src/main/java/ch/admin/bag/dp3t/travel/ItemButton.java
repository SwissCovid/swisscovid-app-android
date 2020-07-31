package ch.admin.bag.dp3t.travel;

import android.view.View;

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

}
