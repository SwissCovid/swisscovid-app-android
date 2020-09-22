/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.home.model.TracingState;

public class TracingStatusHelper {

	public static void updateStatusView(View statusView, TracingState state, boolean isHomeFragment) {
		updateStatusView(statusView, state, true, isHomeFragment);
	}

	public static void updateStatusView(View statusView, TracingState state, boolean displayIllu, boolean isHomeFragment) {
		Context context = statusView.getContext();
		if (TracingState.getBackgroundColor(state) != -1) {
			statusView.findViewById(R.id.status_background)
					.setBackgroundTintList(
							ColorStateList.valueOf(ContextCompat.getColor(context, TracingState.getBackgroundColor(state))));
		}
		ImageView iconView = statusView.findViewById(R.id.status_icon);
		TextView titleView = statusView.findViewById(R.id.status_title);
		TextView textView = statusView.findViewById(R.id.status_text);
		ImageView illustrationView = statusView.findViewById(R.id.status_illustration);
		LinearLayout backgroundTracingInfo = statusView.findViewById(R.id.status_tracing_background_info);
		int color = ContextCompat.getColor(context, TracingState.getTextColor(state));
		if (color != -1) {
			titleView.setTextColor(color);
		}
		if (TracingState.getTitle(state) != -1) {
			titleView.setText(TracingState.getTitle(state));
			titleView.setVisibility(View.VISIBLE);
		} else {
			titleView.setVisibility(View.GONE);
		}
		if (TracingState.getText(state, isHomeFragment) != -1) {
			textView.setText(TracingState.getText(state, isHomeFragment));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
		if (TracingState.getIcon(state) != -1) {
			iconView.setImageResource(TracingState.getIcon(state));
			iconView.setVisibility(View.VISIBLE);
		} else {
			iconView.setVisibility(View.GONE);
		}
		iconView.setImageTintList(ColorStateList.valueOf(color));
		if (displayIllu && TracingState.getIllu(state) != -1) {
			illustrationView.setImageResource(TracingState.getIllu(state));
			illustrationView.setVisibility(View.VISIBLE);
		} else {
			illustrationView.setVisibility(View.GONE);
		}
		if (state.equals(TracingState.ACTIVE)) {
			backgroundTracingInfo.setVisibility(View.VISIBLE);
		} else {
			backgroundTracingInfo.setVisibility(View.GONE);
		}
	}

	public static void showTracingDeactivated(View tracingErrorView, boolean isHomeFragment) {
		ImageView iconView = tracingErrorView.findViewById(R.id.error_status_image);
		if (TracingState.getIcon(TracingState.NOT_ACTIVE) != -1) {
			iconView.setImageResource(TracingState.getIcon(TracingState.NOT_ACTIVE));
			iconView.setVisibility(View.VISIBLE);
		} else {
			iconView.setVisibility(View.GONE);
		}
		TextView titleView = tracingErrorView.findViewById(R.id.error_status_title);
		if (TracingState.getTitle(TracingState.NOT_ACTIVE) != -1) {
			titleView.setText(TracingState.getTitle(TracingState.NOT_ACTIVE));
			titleView.setVisibility(View.VISIBLE);
		} else {
			titleView.setVisibility(View.GONE);
		}

		TextView textView = tracingErrorView.findViewById(R.id.error_status_text);
		if (TracingState.getText(TracingState.NOT_ACTIVE, isHomeFragment) != -1) {
			textView.setText(TracingState.getText(TracingState.NOT_ACTIVE, isHomeFragment));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
		tracingErrorView.findViewById(R.id.error_status_code).setVisibility(View.GONE);
		TextView buttonView = tracingErrorView.findViewById(R.id.error_status_button);
		if (isHomeFragment) {
			buttonView.setText(R.string.activate_tracing_button);
			buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			buttonView.setVisibility(View.VISIBLE);
		} else {
			buttonView.setVisibility(View.GONE);
		}
	}

}
