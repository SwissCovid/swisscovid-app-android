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

	public static void updateStatusView(View statusView, NotificationState state, long daySinceExposed) {
		Context context = statusView.getContext();
		if (NotificationState.getBackgroundColor(state) != -1) {
			statusView.findViewById(R.id.status_background)
					.setBackgroundTintList(
							ColorStateList.valueOf(ContextCompat.getColor(context, NotificationState.getBackgroundColor(state))));
		}
		ImageView iconView = statusView.findViewById(R.id.status_icon);
		TextView titleView = statusView.findViewById(R.id.status_title);
		TextView textView = statusView.findViewById(R.id.status_text);
		ImageView illustrationView = statusView.findViewById(R.id.status_illustration);
		int titleColor = ContextCompat.getColor(context, NotificationState.getTitleTextColor(state));
		int textColor = ContextCompat.getColor(context, NotificationState.geTextColor(state));
		titleView.setTextColor(titleColor);
		textView.setTextColor(textColor);
		iconView.setImageTintList(ColorStateList.valueOf(titleColor));

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
		ImageView triangle = statusView.findViewById(R.id.status_triangle);
		triangle.setVisibility(View.GONE);
		View infoContainer = statusView.findViewById(R.id.status_additional_info);
		infoContainer.setVisibility(View.GONE);
		TextView infoText = statusView.findViewById(R.id.status_additional_info_text);
		TextView infoTel = statusView.findViewById(R.id.status_additional_info_tel);
		TextView infoSince = statusView.findViewById(R.id.status_additional_info_since);

		if (state.equals(NotificationState.EXPOSED)) {
			triangle.setVisibility(View.VISIBLE);
			triangle.setImageResource(R.drawable.triangle_status_exposed);
			infoContainer.setVisibility(View.VISIBLE);
			infoText.setText(R.string.exposed_info_contact_hotline);
			infoTel.setText(R.string.meldungen_detail_call_text);
			infoSince.setVisibility(View.VISIBLE);
			if (daySinceExposed == 0) {
				infoSince.setText(R.string.date_today);
			} else if (daySinceExposed == 1) {
				infoSince.setText(R.string.date_one_day_ago);
			} else if (daySinceExposed > 1) {
				String text = context.getString(R.string.date_days_ago).replace("{COUNT}", String.valueOf(daySinceExposed));
				infoSince.setText(text);
			} else {
				infoSince.setVisibility(View.GONE);
			}
		} else if (state.equals(NotificationState.POSITIVE_TESTED)) {
			triangle.setVisibility(View.VISIBLE);
			triangle.setImageResource(R.drawable.triangle_status_infected);
			infoContainer.setVisibility(View.VISIBLE);
			infoText.setText(R.string.meldung_homescreen_positive_info_line1);
			infoTel.setText(R.string.meldung_homescreen_positive_info_line2);
			infoSince.setVisibility(View.GONE);
		}
	}

	public static void updateStatusView(View reportStatusView, NotificationState exposed) {
		updateStatusView(reportStatusView, exposed, -1);
	}

}
