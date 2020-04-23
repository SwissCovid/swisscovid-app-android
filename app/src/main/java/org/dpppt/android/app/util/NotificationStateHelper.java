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
		statusView.setBackgroundColor(ContextCompat.getColor(context, NotificationState.getBackgroundColor(state)));
		ImageView iconView = statusView.findViewById(R.id.status_icon);
		TextView titleView = statusView.findViewById(R.id.status_title);
		TextView textView = statusView.findViewById(R.id.status_text);
		int color = ContextCompat.getColor(context, NotificationState.getTextColor(state));
		titleView.setText(NotificationState.getTitle(state));
		titleView.setTextColor(color);
		textView.setText(NotificationState.getText(state));
		textView.setTextColor(color);
		iconView.setImageResource(NotificationState.getIcon(state));
		iconView.setImageTintList(ColorStateList.valueOf(color));
	}

}
