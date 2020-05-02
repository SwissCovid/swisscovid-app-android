/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.networking.errors;

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
