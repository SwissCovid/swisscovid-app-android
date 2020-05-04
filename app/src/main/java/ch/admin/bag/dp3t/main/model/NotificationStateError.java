/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package ch.admin.bag.dp3t.main.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import ch.admin.bag.dp3t.R;

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
