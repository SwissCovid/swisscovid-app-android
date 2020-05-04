/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.networking.models;

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
