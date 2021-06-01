/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.inform;

import ch.admin.bag.dp3t.R;

public enum InformRequestError {
	BLACK_INVALID_AUTH_RESPONSE_FORM(R.string.unexpected_error_title, "ABIONDT"),
	BLACK_STATUS_ERROR(R.string.unexpected_error_title, "ABST"),
	BLACK_MISC_NETWORK_ERROR(R.string.network_error, "ABNETWE"),
	RED_STATUS_ERROR(R.string.unexpected_error_with_retry, "ARST"),
	RED_USER_CANCELLED_SHARE(R.string.user_cancelled_key_sharing_error, "ARUSCCD"),
	RED_EXPOSURE_API_ERROR(R.string.unexpected_error_title, "AREA"),
	RED_MISC_NETWORK_ERROR(R.string.network_error, "ARNETWE"),
	USER_UPLOAD_NETWORK_ERROR(R.string.network_error, "AUANETWE"),
	USER_UPLOAD_UNKONWN_ERROR(R.string.network_error, "AUAUKWE");


	private final int errorMessage;
	private final String errorCode;


	InformRequestError(int errorMessage, String errorCode) {
		this.errorMessage = errorMessage;
		this.errorCode = errorCode;
	}

	public int getErrorMessage() {
		return errorMessage;
	}

	public String getErrorCode(String addCode) {
		if (errorCode == null) return null;
		return addCode == null ? errorCode : errorCode + addCode;
	}
}
