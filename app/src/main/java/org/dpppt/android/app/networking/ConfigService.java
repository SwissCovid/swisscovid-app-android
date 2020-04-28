/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.networking;

import org.dpppt.android.app.networking.models.ConfigResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ConfigService {

	@Headers({
					 "accept: */*",
					 "content-type: application/json"
			 })
	@GET("v1/config")
	Call<ConfigResponseModel> getConfig(@Query("appversion") String appVersion, @Query("osversion") String osVersion);

}
