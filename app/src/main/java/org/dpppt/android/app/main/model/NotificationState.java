package org.dpppt.android.app.main.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.dpppt.android.app.R;

public enum NotificationState {
	NO_NOTIFICATION,
	EXPOSED,
	POSITIVE_TESTED;

	public static @StringRes
	int getTitle(NotificationState notificationState) {
		switch (notificationState) {
			case NO_NOTIFICATION:
				return R.string.meldungen_no_meldungen_title;
			case EXPOSED:
				return R.string.meldungen_meldung_title;
			case POSITIVE_TESTED:
				return R.string.meldungen_infected_title;
		}
		throw new IllegalStateException("Unknown State");
	}

	public static @StringRes
	int getText(NotificationState NotificationState) {
		switch (NotificationState) {
			case NO_NOTIFICATION:
				return R.string.meldungen_no_meldungen_text;
			case EXPOSED:
				return R.string.meldungen_meldung_text;
			case POSITIVE_TESTED:
				return R.string.meldungen_infected_text;
		}
		throw new IllegalStateException("Unknown State");
	}

	public static @DrawableRes
	int getIcon(NotificationState notificationState) {
		switch (notificationState) {
			case NO_NOTIFICATION:
				return R.drawable.ic_check;
			case EXPOSED:
				return R.drawable.ic_check;
			case POSITIVE_TESTED:
				return R.drawable.ic_info;
		}
		throw new IllegalStateException("Unknown State");
	}

	public static @ColorRes
	int getTextColor(NotificationState notificationState) {
		switch (notificationState) {
			case NO_NOTIFICATION:
				return R.color.green_main;
			case EXPOSED:
				return R.color.white;
			case POSITIVE_TESTED:
				return R.color.white;
		}
		return -1;
	}

	public static @ColorRes
	int getBackgroundColor(NotificationState notificationState) {
		switch (notificationState) {
			case NO_NOTIFICATION:
				return R.color.status_green_bg;
			case EXPOSED:
				return R.color.blue_main;
			case POSITIVE_TESTED:
				return R.color.purple_main;
		}
		return -1;
	}
}
