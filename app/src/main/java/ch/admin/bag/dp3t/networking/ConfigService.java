/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.networking;

import ch.admin.bag.dp3t.networking.models.ConfigResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ConfigService {

	@Headers("Accept: application/json")
	@GET("v1/config")
	Call<ConfigResponseModel> getConfig(
			@Query("appversion") String appVersion,
			@Query("osversion") String osVersion,
			@Query("buildnr") String buildNumber
	);

}
