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
			TracingStatus.ErrorState.BATTERY_OPTIMIZER_ENABLED,
			TracingStatus.ErrorState.BLE_INTERNAL_ERROR,
			TracingStatus.ErrorState.BLE_ADVERTISING_ERROR,
			TracingStatus.ErrorState.BLE_SCANNER_ERROR,
			TracingStatus.ErrorState.NETWORK_ERROR_WHILE_SYNCING);

	private static final List<TracingStatus.ErrorState> possibleNotificationErrorStatesOrderedByPriority = Arrays.asList();

	public static @StringRes
	int getTitle(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case BLE_DISABLED:
				return R.string.bluetooth_turned_off_title;
			case MISSING_LOCATION_PERMISSION:
				return R.string.grant_permission_button;
			/*missing
			case ERROR_LOCATION_OFF:
				return -1;*/
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.loading_view_error_title;
				/*missing
			case ERROR_TIMING_INCONSISTENCY:
				return -1;*/
			case NETWORK_ERROR_WHILE_SYNCING:
				return R.string.loading_view_error_title;
				/*
				SDK error missing
				* */
		}
		return -1;
	}

	public static @StringRes
	int getText(TracingStatus.ErrorState tracingErrorState) {
		return tracingErrorState.getErrorString();
	}

	public static @DrawableRes
	int getIcon(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case BLE_DISABLED:
				return R.drawable.ic_bluetooth_off;
			case MISSING_LOCATION_PERMISSION:
				return R.drawable.ic_location_off_red;
			/*missing
			case ERROR_LOCATION_OFF:
				return -1;*/
			case BATTERY_OPTIMIZER_ENABLED:
				return R.drawable.ic_warning_red;
			/*missing
			case ERROR_TIMING_INCONSISTENCY:
				return -1;*/
			case NETWORK_ERROR_WHILE_SYNCING:
				return R.drawable.ic_warning_red;
			/*
				SDK error missing
				* */
		}
		return -1;
	}

	public static @StringRes
	int getButtonText(TracingStatus.ErrorState errorState) {
		switch (errorState) {
			case BLE_DISABLED:
				return R.string.bluetooth_turn_on_button_title;
			case MISSING_LOCATION_PERMISSION:
				return R.string.grant_permission_button;
			/*missing
			case ERROR_LOCATION_OFF:
				return -1;*/
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.button_battery_optimization_deactivated;
			/*missing
			case ERROR_TIMING_INCONSISTENCY:
				return -1;*/
			case NETWORK_ERROR_WHILE_SYNCING:
				//no action
				return -1;
			/*
				SDK error missing
				* */
		}
		return -1;
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

		if (TracingErrorStateHelper.getIcon(errorState) != -1) {
			iconView.setImageResource(TracingErrorStateHelper.getIcon(errorState));
			iconView.setVisibility(View.VISIBLE);
		} else {
			iconView.setVisibility(View.GONE);
		}
		if (TracingErrorStateHelper.getTitle(errorState) != -1) {
			titleView.setText(TracingErrorStateHelper.getTitle(errorState));
			titleView.setVisibility(View.VISIBLE);
		} else {
			titleView.setVisibility(View.GONE);
		}
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
