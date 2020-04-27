/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.networking;

import org.dpppt.android.app.networking.models.AuthenticationCodeRequestModel;
import org.dpppt.android.app.networking.models.AuthenticationCodeResponseModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthCodeService {

	@Headers({
					 "accept: */*",
					 "content-type: application/json"
			 })
	@POST("v1/onset")
	Call<AuthenticationCodeResponseModel> getAccessToken(@Body AuthenticationCodeRequestModel code);

}
