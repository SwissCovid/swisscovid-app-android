package ch.admin.bag.dp3t.checkin.models;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import ch.admin.bag.dp3t.R;

public enum CrowdNotifierErrorState {
	NETWORK(R.string.error_network_title, R.string.error_network_text, R.string.error_action_retry, R.drawable.ic_error),
	CAMERA_ACCESS_DENIED(R.string.error_camera_permission_title, R.string.error_camera_permission_text,
			R.string.error_action_change_settings, R.drawable.ic_cam_off),
	NO_VALID_QR_CODE(R.string.error_title, R.string.qrscanner_error, R.string.android_button_ok, R.drawable.ic_error),
	QR_CODE_NOT_YET_VALID(R.string.error_title, R.string.qr_scanner_error_code_not_yet_valid, R.string.android_button_ok,
			R.drawable.ic_error),
	QR_CODE_NOT_VALID_ANYMORE(R.string.error_title, R.string.qr_scanner_error_code_not_valid_anymore, R.string.android_button_ok,
			R.drawable.ic_error),
	ALREADY_CHECKED_IN(R.string.error_title, R.string.error_already_checked_in, R.string.android_button_ok, R.drawable.ic_error),
	UPDATE_REQUIRED(R.string.error_update_title, R.string.error_update_text, R.string.error_action_update,
			R.drawable.ic_error),
	ONLY_INSTANT_ONBOARDING_DONE(R.string.tracing_turned_off_title, R.string.partial_onboarding_box_text,
			R.string.partial_onboarding_box_action, R.drawable.ic_info);


	@StringRes private int titleResId;
	@StringRes private int textResId;
	@StringRes private int actionResId;
	@DrawableRes private int imageResId;

	CrowdNotifierErrorState(@StringRes int titleResId, @StringRes int textResId, @StringRes int actionResId,
			@DrawableRes int imageResId) {
		this.titleResId = titleResId;
		this.textResId = textResId;
		this.actionResId = actionResId;
		this.imageResId = imageResId;
	}

	public int getTitleResId() {
		return titleResId;
	}

	public int getTextResId() {
		return textResId;
	}

	public int getActionResId() {
		return actionResId;
	}

	public int getImageResId() {
		return imageResId;
	}
}
