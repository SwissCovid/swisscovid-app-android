/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.model.TracingState;

public class TracingStatusHelper {


	public static void updateStatusView(View statusView, TracingState state) {
		Context context = statusView.getContext();
		statusView.setBackgroundColor(ContextCompat.getColor(context, TracingState.getBackgroundColor(state)));
		ImageView iconView = statusView.findViewById(R.id.status_icon);
		TextView titleView = statusView.findViewById(R.id.status_title);
		TextView textView = statusView.findViewById(R.id.status_text);
		int color = ContextCompat.getColor(context, TracingState.getTextColor(state));
		titleView.setText(TracingState.getTitle(state));
		titleView.setTextColor(color);
		textView.setText(TracingState.getText(state));
		textView.setTextColor(color);
		iconView.setImageResource(TracingState.getIcon(state));
		iconView.setImageTintList(ColorStateList.valueOf(color));
	}

	public static void showTracingDeactivated(View tracingErrorView) {
		ImageView iconView = tracingErrorView.findViewById(R.id.error_status_image);
		iconView.setImageResource(TracingState.getIcon(TracingState.NOT_ACTIVE));
		TextView titleView = tracingErrorView.findViewById(R.id.error_status_title);
		titleView.setText(TracingState.getTitle(TracingState.NOT_ACTIVE));
		TextView textView = tracingErrorView.findViewById(R.id.error_status_text);
		textView.setText(TracingState.getText(TracingState.NOT_ACTIVE));
		TextView buttonView = tracingErrorView.findViewById(R.id.error_status_button);
		buttonView.setVisibility(View.GONE);
	}

}
