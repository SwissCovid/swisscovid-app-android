package org.dpppt.android.app.util;

import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dpppt.android.app.R;
import org.dpppt.android.sdk.TracingStatus;

public class TracingErrorStateHelper {

	private static final List<TracingStatus.ErrorState> possibleErrorStatesOrderedByPriority = Arrays.asList(
			TracingStatus.ErrorState.BLE_NOT_SUPPORTED,
			TracingStatus.ErrorState.MISSING_LOCATION_PERMISSION,
			TracingStatus.ErrorState.BLE_DISABLED,
			TracingStatus.ErrorState.LOCATION_SERVICE_DISABLED,
			TracingStatus.ErrorState.BATTERY_OPTIMIZER_ENABLED,
			TracingStatus.ErrorState.BLE_INTERNAL_ERROR,
			TracingStatus.ErrorState.BLE_ADVERTISING_ERROR,
			TracingStatus.ErrorState.BLE_SCANNER_ERROR);

	private static final List<TracingStatus.ErrorState> possibleNotificationErrorStatesOrderedByPriority = Arrays.asList(
			TracingStatus.ErrorState.NETWORK_ERROR_WHILE_SYNCING
	);

	private static @StringRes
	int getTitle(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case LOCATION_SERVICE_DISABLED:
				return R.string.error_location_services_title;
			case BLE_DISABLED:
				return R.string.bluetooth_turned_off_title;
			case MISSING_LOCATION_PERMISSION:
				return R.string.error_location_permission_title;
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.error_battery_optimization_title;
			case BLE_NOT_SUPPORTED:
			case BLE_INTERNAL_ERROR:
			case BLE_ADVERTISING_ERROR:
			case BLE_SCANNER_ERROR:
			case NETWORK_ERROR_WHILE_SYNCING:
			default:
				return R.string.begegnungen_restart_error_title;

		}
	}

	private static @StringRes
	int getText(TracingStatus.ErrorState tracingErrorState) {
		return tracingErrorState.getErrorString();
	}

	private static @DrawableRes
	int getIcon(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case LOCATION_SERVICE_DISABLED:
				return R.drawable.ic_gps_off;
			case BLE_DISABLED:
				return R.drawable.ic_bluetooth_off;
			case MISSING_LOCATION_PERMISSION:
				return R.drawable.ic_location_off_red;
			case BATTERY_OPTIMIZER_ENABLED:
				return R.drawable.ic_battery;
			case BLE_NOT_SUPPORTED:
			case BLE_INTERNAL_ERROR:
			case BLE_ADVERTISING_ERROR:
			case BLE_SCANNER_ERROR:
			case NETWORK_ERROR_WHILE_SYNCING:
			default:
				return R.drawable.ic_warning_red;
		}
	}

	private static @StringRes
	int getButtonText(TracingStatus.ErrorState errorState) {
		switch (errorState) {
			case LOCATION_SERVICE_DISABLED:
				return R.string.error_location_services_button;
			case BLE_DISABLED:
				return R.string.bluetooth_turn_on_button_title;
			case MISSING_LOCATION_PERMISSION:
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.error_location_permission_button;
			case NETWORK_ERROR_WHILE_SYNCING:
				return R.string.homescreen_meldung_data_outdated_retry_button;
			case BLE_NOT_SUPPORTED:
			case BLE_INTERNAL_ERROR:
			case BLE_ADVERTISING_ERROR:
			case BLE_SCANNER_ERROR:
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

	public static void updateErrorView(View tracingErrorView, TracingStatus.ErrorState errorState) {
		if (errorState == null) {
			tracingErrorView.setVisibility(View.GONE);
			return;
		}
		tracingErrorView.setVisibility(View.VISIBLE);
		ImageView iconView = tracingErrorView.findViewById(R.id.error_status_image);
		TextView titleView = tracingErrorView.findViewById(R.id.error_status_title);
		TextView textView = tracingErrorView.findViewById(R.id.error_status_text);
		TextView buttonView = tracingErrorView.findViewById(R.id.error_status_button);

		iconView.setImageResource(TracingErrorStateHelper.getIcon(errorState));
		iconView.setVisibility(View.VISIBLE);

		titleView.setText(TracingErrorStateHelper.getTitle(errorState));
		titleView.setVisibility(View.VISIBLE);

		if (TracingErrorStateHelper.getText(errorState) != -1) {
			textView.setText(TracingErrorStateHelper.getText(errorState));
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}

		if (TracingErrorStateHelper.getButtonText(errorState) != -1) {
			buttonView.setText(TracingErrorStateHelper.getButtonText(errorState));
			buttonView.setVisibility(View.VISIBLE);
			buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		} else {
			buttonView.setVisibility(View.GONE);
		}
	}



}
