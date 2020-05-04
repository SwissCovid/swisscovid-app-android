/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.networking;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.security.PublicKey;

import ch.admin.bag.dp3t.networking.errors.ResponseError;
import ch.admin.bag.dp3t.networking.models.ConfigResponseModel;
import ch.admin.bag.dp3t.BuildConfig;

import org.dpppt.android.sdk.backend.SignatureVerificationInterceptor;
import org.dpppt.android.sdk.util.SignatureUtil;

import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigRepository {

	private static CertificatePinner certificatePinner = CertificatePinner.DEFAULT;

	public static void setCertificatePinner(@NonNull CertificatePinner certificatePinner) {
		ConfigRepository.certificatePinner = certificatePinner;
	}

	private ConfigService configService;

	public ConfigRepository(@NonNull Context context) {
		OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();

		PublicKey publicKey = SignatureUtil.getPublicKeyFromCertificateBase64OrThrow(BuildConfig.CONFIG_CERTIFICATE);
		okHttpBuilder.addInterceptor(new SignatureVerificationInterceptor(publicKey));

		int cacheSize = 5 * 1024 * 1024; // 5 MB
		Cache cache = new Cache(context.getCacheDir(), cacheSize);
		okHttpBuilder.cache(cache);

		okHttpBuilder.certificatePinner(certificatePinner);

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BuildConfig.CONFIG_URL)
				.client(okHttpBuilder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		configService = retrofit.create(ConfigService.class);
	}

	public ConfigResponseModel getConfig(@NonNull String appVersion, @NonNull String osVersion, @NonNull String buildNumber)
			throws IOException, ResponseError {
		Response<ConfigResponseModel> configResponse = configService.getConfig(appVersion, osVersion, buildNumber).execute();
		if (configResponse.isSuccessful()) {
			return configResponse.body();
		} else {
			throw new ResponseError(configResponse.raw());
		}
	}

}
