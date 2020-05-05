/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.networking.errors;

import androidx.annotation.NonNull;

import okhttp3.Response;

public class ResponseError extends Throwable {

	private Response response;

	public ResponseError(@NonNull Response response) {
		this.response = response;
	}

	public int getStatusCode() {
		return response.code();
	}

}
