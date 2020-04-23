package org.dpppt.android.app.main.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.dpppt.android.app.R;
import org.dpppt.android.sdk.TracingStatus;

public class TracingErrorStateHelper {

	public static @StringRes
	int getTitle(TracingStatus.ErrorState tracingErrorState) {
		switch (tracingErrorState) {
			case BLE_DISABLED:
				return R.string.bluetooth_turned_off_title;
			case MISSING_LOCATION_PERMISSION:
				return -1;
			/*missing
			case ERROR_LOCATION_OFF:
				return -1;*/
			case BATTERY_OPTIMIZER_ENABLED:
				return R.string.button_battery_optimization_deactivated;
				/*missing
			case ERROR_TIMING_INCONSISTENCY:
				return -1;*/
			case NETWORK_ERROR_WHILE_SYNCING:
				return -1;
				/*
				SDK error missing
				* */
		}
		throw new IllegalStateException("Unknown State");
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
				return -1;
			/*missing
			case ERROR_LOCATION_OFF:
				return -1;*/
			case BATTERY_OPTIMIZER_ENABLED:
				return R.drawable.ic_warning;
			/*missing
			case ERROR_TIMING_INCONSISTENCY:
				return -1;*/
			case NETWORK_ERROR_WHILE_SYNCING:
				return R.drawable.ic_warning;
			/*
				SDK error missing
				* */
		}
		throw new IllegalStateException("Unknown State");
	}

}
