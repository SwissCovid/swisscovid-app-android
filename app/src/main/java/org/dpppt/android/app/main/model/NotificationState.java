package org.dpppt.android.app.main.model;

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
				return -1;
			case EXPOSED:
				return -1;
			case POSITIVE_TESTED:
				return R.string.inform_button_positive_title;
		}
		throw new IllegalStateException("Unknown State");
	}

	public static @StringRes
	int getText(NotificationState NotificationState) {
		switch (NotificationState) {
			case NO_NOTIFICATION:
				return -1;
			case EXPOSED:
				return -1;
			case POSITIVE_TESTED:
				return R.string.inform_button_positive_text;
		}
		throw new IllegalStateException("Unknown State");
	}

	public static @DrawableRes
	int getIcon(NotificationState notificationState) {
		switch (notificationState) {
			case NO_NOTIFICATION:
				return R.drawable.ic_info;
			case EXPOSED:
				return R.drawable.ic_info;
			case POSITIVE_TESTED:
				return R.drawable.ic_info;
		}
		throw new IllegalStateException("Unknown State");
	}
}
