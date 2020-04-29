package org.dpppt.android.app.main.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.dpppt.android.app.R;

public enum NotificationStateError {
	NOTIFICATION_STATE_ERROR;


	@StringRes public static int getTitle(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.string.meldungen_background_error_title;
			default:
				return -1;
		}
	}

	@StringRes public static int getText(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.string.meldungen_background_error_text;
			default:
				return -1;
		}
	}

	@DrawableRes public static int getIcon(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.drawable.ic_refresh;
			default:
				return -1;
		}
	}

	@StringRes public static int getButtonText(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.string.meldungen_background_error_button;
			default:
				return -1;
		}
	}
}
