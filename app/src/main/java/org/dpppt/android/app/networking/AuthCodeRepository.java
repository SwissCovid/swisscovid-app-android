/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.networking;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.IOException;

import org.dpppt.android.app.BuildConfig;
import org.dpppt.android.app.networking.errors.InvalidCodeError;
import org.dpppt.android.app.networking.errors.ResponseError;
import org.dpppt.android.app.networking.models.AuthenticationCodeRequestModel;
import org.dpppt.android.app.networking.models.AuthenticationCodeResponseModel;
import org.dpppt.android.sdk.backend.ResponseCallback;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthCodeRepository {

	private AuthCodeService authCodeService;

	public AuthCodeRepository(@NonNull Context context) {

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
				.baseUrl(BuildConfig.AUTH_CODE_URL)
				.client(okHttpBuilder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		authCodeService = retrofit.create(AuthCodeService.class);
	}

	public void getAccessToken(@NonNull AuthenticationCodeRequestModel authenticationCode,
			@NonNull ResponseCallback<AuthenticationCodeResponseModel> callbackListener) {

		authCodeService.getAccessToken(authenticationCode).enqueue(new Callback<AuthenticationCodeResponseModel>() {
			@Override
			public void onResponse(Call<AuthenticationCodeResponseModel> call,
					Response<AuthenticationCodeResponseModel> response) {
				if (response.isSuccessful()) {
					callbackListener.onSuccess(response.body());
				} else {
					if (response.code() == 404) {
						onFailure(call, new InvalidCodeError());
					} else {
						onFailure(call, new ResponseError(response.raw()));
					}
				}
			}

			@Override
			public void onFailure(Call<AuthenticationCodeResponseModel> call, Throwable t) {
				callbackListener.onError(t);
			}
		});
	}

	public AuthenticationCodeResponseModel getAccessTokenSync(@NonNull AuthenticationCodeRequestModel authenticationCode)
			throws IOException, ResponseError {
		Response<AuthenticationCodeResponseModel> response = authCodeService.getAccessToken(authenticationCode).execute();
		if (!response.isSuccessful()) throw new ResponseError(response.raw());
		return response.body();
	}

}
