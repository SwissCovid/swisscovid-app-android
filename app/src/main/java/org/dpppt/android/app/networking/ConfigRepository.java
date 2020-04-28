/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.networking;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Random;

import org.dpppt.android.app.BuildConfig;
import org.dpppt.android.app.networking.errors.ResponseError;
import org.dpppt.android.app.networking.models.ConfigResponseModel;
import org.dpppt.android.app.networking.models.InfoBoxModel;
import org.dpppt.android.sdk.backend.ResponseCallback;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigRepository {

	private ConfigService configService;

	public ConfigRepository(@NonNull Context context) {

		OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
		okHttpBuilder.addInterceptor(chain -> {
			Request request = chain.request()
					.newBuilder()
					.build();
			return chain.proceed(request);
		});

		int cacheSize = 5 * 1024 * 1024; // 5 MB
		Cache cache = new Cache(context.getCacheDir(), cacheSize);
		okHttpBuilder.cache(cache);

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BuildConfig.CONFIG_URL)
				.client(okHttpBuilder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		configService = retrofit.create(ConfigService.class);
	}

	public ConfigResponseModel getConfig(@NonNull String appVersion, @NonNull String osVersion) throws IOException, ResponseError {
		int rand = new Random(System.currentTimeMillis()).nextInt(4);
		switch (rand) {
			case 0:
				return new ConfigResponseModel(true, null);
			case 1:
				return new ConfigResponseModel(false, new InfoBoxModel("Lorem Ipsum",
						"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam "
								+ " nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
						"https://github.com/DP-3T", "Link zum DP-3T Repository"));
			default:
				return new ConfigResponseModel(false, null);
		}
		/*Response<ConfigResponseModel> configResponse = configService.getConfig(appVersion, osVersion).execute();
		if (configResponse.isSuccessful()) {
			return configResponse.body();
		} else {
			throw new ResponseError(configResponse.raw());
		}*/
	}

}
