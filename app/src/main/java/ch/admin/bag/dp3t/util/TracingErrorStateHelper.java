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
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dpppt.android.sdk.TracingStatus;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState;

public class TracingErrorStateHelper {

	private static final List<TracingStatus.ErrorState> possibleErrorStatesOrderedByPriority = Arrays.asList(
			TracingStatus.ErrorState.GAEN_NOT_AVAILABLE,
			TracingStatus.ErrorState.GAEN_UNEXPECTEDLY_DISABLED,
			TracingStatus.ErrorState.BLE_NOT_SUPPORTED,
			TracingStatus.ErrorState.BLE_DISABLED,
			TracingStatus.ErrorState.LOCATION_SERVICE_DISABLED,
			TracingStatus.ErrorState.BATTERY_OPTIMIZER_ENABLED,
			TracingStatus.ErrorState.SYNC_ERROR_TIMING);

	private static final List<TracingStatus.ErrorState> possibleNotificationErrorStatesOrderedByPriority = Arrays.asList(
			TracingStatus.ErrorState.SYNC_ERROR_API_EXCEPTION,
			TracingStatus.ErrorState.SYNC_ERROR_SERVER,
			TracingStatus.ErrorState.SYNC_ERROR_NO_SPACE,
			TracingStatus.ErrorState.SYNC_ERROR_NETWORK,
			TracingStatus.ErrorState.SYNC_ERROR_SSLTLS,
			TracingStatus.ErrorState.SYNC_ERROR_SIGNATURE
	);

	@StringRes
	private static int getTitle(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.android_error_battery_optimization_title;
			case LOCATION_SERVICE_DISABLED:
				return R.string.android_error_location_services_title;
			case BLE_DISABLED:
				return R.string.bluetooth_turned_off_title;
			case SYNC_ERROR_TIMING:
				return R.string.time_inconsistency_title;
			case GAEN_NOT_AVAILABLE:
				return R.string.gaen_not_aviable;
			case GAEN_UNEXPECTEDLY_DISABLED:
				return R.string.bluetooth_setting_tracking_inactive;
			case SYNC_ERROR_SERVER:
			case SYNC_ERROR_NETWORK:
			case SYNC_ERROR_SSLTLS:
			case SYNC_ERROR_NO_SPACE:
			case SYNC_ERROR_SIGNATURE:
			case SYNC_ERROR_API_EXCEPTION:
				return R.string.homescreen_meldung_data_outdated_title;
			case BLE_NOT_SUPPORTED:
			default:
				return R.string.begegnungen_restart_error_title;
		}
	}

	private static String getText(Context context, TracingStatus.ErrorState tracingErrorState) {
		return tracingErrorState.getErrorString(context);
	}

	@DrawableRes
	private static int getIcon(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case BATTERY_OPTIMIZER_ENABLED:
				return R.drawable.ic_battery_power;
			case LOCATION_SERVICE_DISABLED:
				return R.drawable.ic_gps_off;
			case BLE_DISABLED:
				return R.drawable.ic_bluetooth_off;
			case SYNC_ERROR_TIMING:
				return R.drawable.ic_time;
			case GAEN_NOT_AVAILABLE:
			case GAEN_UNEXPECTEDLY_DISABLED:
			case SYNC_ERROR_SERVER:
			case SYNC_ERROR_NETWORK:
			case SYNC_ERROR_SSLTLS:
			case SYNC_ERROR_SIGNATURE:
			case SYNC_ERROR_API_EXCEPTION:
			case SYNC_ERROR_NO_SPACE:
			case BLE_NOT_SUPPORTED:
			default:
				return R.drawable.ic_warning_red;
		}
	}

	@StringRes
	private static int getButtonText(TracingStatus.ErrorState errorState) {
		switch (errorState) {
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.android_error_battery_optimization_button;
			case LOCATION_SERVICE_DISABLED:
				return R.string.android_error_location_services_button;
			case BLE_DISABLED:
				return R.string.bluetooth_turn_on_button_title;
			case GAEN_UNEXPECTEDLY_DISABLED:
				return R.string.onboarding_gaen_button_activate;
			case SYNC_ERROR_SERVER:
			case SYNC_ERROR_NETWORK:
			case SYNC_ERROR_SSLTLS:
			case SYNC_ERROR_SIGNATURE:
			case SYNC_ERROR_NO_SPACE:
				return R.string.homescreen_meldung_data_outdated_retry_button;
			case GAEN_NOT_AVAILABLE:
				return R.string.playservices_update;
			case SYNC_ERROR_TIMING:
			case BLE_NOT_SUPPORTED:
			case SYNC_ERROR_API_EXCEPTION:
			default:
				return -1;
		}
	}

	public static TracingStatus.ErrorState getErrorState(Collection<TracingStatus.ErrorState> errors) {
		for (TracingStatus.ErrorState errorState : possibleErrorStatesOrderedByPriority) {
			if (errors.contains(errorState)) {
				return errorState;
			}
		}
		return null;
	}

	public static TracingStatus.ErrorState getErrorStateForReports(Collection<TracingStatus.ErrorState> errors) {
		for (TracingStatus.ErrorState errorState : possibleNotificationErrorStatesOrderedByPriority) {
			if (errors.contains(errorState)) {
				return errorState;
			}
		}
		return null;
	}

	public static boolean isTracingErrorState(TracingStatus.ErrorState error) {
		return possibleErrorStatesOrderedByPriority.contains(error);
	}

	public static boolean isReportsErrorState(TracingStatus.ErrorState error) {
		return possibleNotificationErrorStatesOrderedByPriority.contains(error);
	}

	public static void hideErrorView(View tracingErrorView) {
		tracingErrorView.setVisibility(View.GONE);
	}

	public static void updateErrorView(View tracingErrorView, CrowdNotifierErrorState errorState) {
		if (errorState == null) {
			tracingErrorView.setVisibility(View.GONE);
			return;
		}
		tracingErrorView.setVisibility(View.VISIBLE);

		updateErrorView(tracingErrorView, errorState.getImageResId(), errorState.getTitleResId(),
				tracingErrorView.getResources().getString(errorState.getTextResId()), "CNNET", errorState.getActionResId());
	}

	public static void updateErrorView(View tracingErrorView, TracingStatus.ErrorState errorState) {
		if (errorState == null) {
			tracingErrorView.setVisibility(View.GONE);
			return;
		}
		tracingErrorView.setVisibility(View.VISIBLE);

		updateErrorView(tracingErrorView, TracingErrorStateHelper.getIcon(errorState),
				TracingErrorStateHelper.getTitle(errorState),
				TracingErrorStateHelper.getText(tracingErrorView.getContext(), errorState),
				TracingErrorStateHelper.getErrorCode(errorState),
				TracingErrorStateHelper.getButtonText(errorState));
	}

	private static void updateErrorView(View tracingErrorView, @DrawableRes int icon, @StringRes int title, String text,
			String errorCodeString, @StringRes int buttonTitle) {
		ImageView iconView = tracingErrorView.findViewById(R.id.error_status_image);
		TextView titleView = tracingErrorView.findViewById(R.id.error_status_title);
		TextView textView = tracingErrorView.findViewById(R.id.error_status_text);
		TextView errorCode = tracingErrorView.findViewById(R.id.error_status_code);
		TextView buttonView = tracingErrorView.findViewById(R.id.error_status_button);

		iconView.setImageResource(icon);
		iconView.setVisibility(View.VISIBLE);
		titleView.setText(title);
		titleView.setVisibility(View.VISIBLE);

		if (text != null) {
			textView.setText(text);
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(errorCodeString)) {
			errorCode.setText(errorCodeString);
			errorCode.setVisibility(View.VISIBLE);
		} else {
			errorCode.setVisibility(View.GONE);
		}
		if (buttonTitle != -1) {
			buttonView.setText(buttonTitle);
			buttonView.setVisibility(View.VISIBLE);
			buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		} else {
			buttonView.setVisibility(View.GONE);
		}
	}

	private static String getErrorCode(TracingStatus.ErrorState errorState) {
		switch (errorState) {
			case BLE_NOT_SUPPORTED:
				return "NSBNS";
			case GAEN_NOT_AVAILABLE:
				return "GANA";
			case GAEN_UNEXPECTEDLY_DISABLED:
				return "GAUD";
			case SYNC_ERROR_SERVER:
				return "RTSES";
			case SYNC_ERROR_NETWORK:
				return "RTSEN" + errorState.getErrorCode();
			case SYNC_ERROR_NO_SPACE:
				return "RTSNS" + errorState.getErrorCode();
			case SYNC_ERROR_SSLTLS:
				return "RTSETLS";
			case SYNC_ERROR_API_EXCEPTION:
				return errorState.getErrorCode();
			case SYNC_ERROR_SIGNATURE:
				return "RTSESI";
			case SYNC_ERROR_TIMING:
			case LOCATION_SERVICE_DISABLED:
			case BLE_DISABLED:
			case BATTERY_OPTIMIZER_ENABLED:
			default:
				return "";
		}
	}

}
