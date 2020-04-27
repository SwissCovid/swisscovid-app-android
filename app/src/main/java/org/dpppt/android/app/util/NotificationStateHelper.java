package org.dpppt.android.app.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.model.NotificationState;

public class NotificationStateHelper {

	public static void updateStatusView(View statusView, NotificationState state) {
		Context context = statusView.getContext();
		if (NotificationState.getBackgroundColor(state) != -1) {
			statusView.findViewById(R.id.status_background).setBackgroundColor(ContextCompat.getColor(context, NotificationState.getBackgroundColor(state)));
		}
		ImageView iconView = statusView.findViewById(R.id.status_icon);
		TextView titleView = statusView.findViewById(R.id.status_title);
		TextView textView = statusView.findViewById(R.id.status_text);
		ImageView illustrationView = statusView.findViewById(R.id.status_illustration);
		int color = ContextCompat.getColor(context, NotificationState.getTextColor(state));
		titleView.setTextColor(color);
		textView.setTextColor(color);
		iconView.setImageTintList(ColorStateList.valueOf(color));

		if (NotificationState.getTitle(state) != -1) {
			titleView.setText(NotificationState.getTitle(state));
			titleView.setVisibility(View.VISIBLE);
		} else {
			titleView.setVisibility(View.GONE);
		}
		if (NotificationState.getText(state) != -1) {
			textView.setText(NotificationState.getText(state));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
		if (NotificationState.getIcon(state) != -1) {
			iconView.setImageResource(NotificationState.getIcon(state));
			iconView.setVisibility(View.VISIBLE);
		} else {
			iconView.setVisibility(View.GONE);
		}
		if (NotificationState.getIllu(state) != -1) {
			illustrationView.setImageResource(NotificationState.getIllu(state));
			illustrationView.setVisibility(View.VISIBLE);
		} else {
			illustrationView.setVisibility(View.GONE);
		}
	}

}
