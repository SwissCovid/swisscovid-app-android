/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.networking.models;

import androidx.annotation.Keep;

public class AuthenticationCodeRequestModel {

	@Keep
	private String authorizationCode;
	@Keep
	private int fake;

	public AuthenticationCodeRequestModel(String authorizationCode, int fake) {
		this.authorizationCode = authorizationCode;
		this.fake = fake;
	}

}
