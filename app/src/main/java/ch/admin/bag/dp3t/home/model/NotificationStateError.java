/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.home.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import ch.admin.bag.dp3t.R;

public enum NotificationStateError {
	NOTIFICATION_STATE_ERROR,
	TRACING_DEACTIVATED;


	@StringRes
	public static int getTitle(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.string.meldungen_background_error_title;
			case TRACING_DEACTIVATED:
				return R.string.meldungen_tracing_turned_off_title;
			default:
				return -1;
		}
	}

	@StringRes
	public static int getText(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.string.meldungen_background_error_text_android;
			case TRACING_DEACTIVATED:
				return R.string.meldungen_tracing_turned_off_warning;
			default:
				return -1;
		}
	}

	@DrawableRes
	public static int getIcon(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.drawable.ic_refresh;
			case TRACING_DEACTIVATED:
				return R.drawable.ic_warning_red;
			default:
				return -1;
		}
	}

	@StringRes
	public static int getButtonText(NotificationStateError notificationStateError) {
		switch (notificationStateError) {
			case NOTIFICATION_STATE_ERROR:
				return R.string.meldungen_background_error_button;
			case TRACING_DEACTIVATED:
				return R.string.activate_tracing_button;
			default:
				return -1;
		}
	}
}
