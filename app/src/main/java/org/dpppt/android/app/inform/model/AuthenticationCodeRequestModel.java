package org.dpppt.android.app.inform.model;

import androidx.annotation.Keep;

public class AuthenticationCodeRequestModel {

	@Keep
	private String authorizationCode;

	public AuthenticationCodeRequestModel(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}

}
