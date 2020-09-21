/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.networking;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.security.PublicKey;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.backend.SignatureException;
import org.dpppt.android.sdk.backend.SignatureVerificationInterceptor;
import org.dpppt.android.sdk.backend.UserAgentInterceptor;
import org.dpppt.android.sdk.util.SignatureUtil;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.networking.errors.ResponseError;
import ch.admin.bag.dp3t.networking.models.StatsResponseModel;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StatsRepository {

	private StatsService statsService;

	public StatsRepository(@NonNull Context context) {
		OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();

		PublicKey publicKey = SignatureUtil.getPublicKeyFromCertificateBase64OrThrow(BuildConfig.CONFIG_CERTIFICATE);
		okHttpBuilder.addInterceptor(new SignatureVerificationInterceptor(publicKey));

		int cacheSize = 5 * 1024 * 1024; // 5 MB
		Cache cache = new Cache(context.getCacheDir(), cacheSize);
		okHttpBuilder.cache(cache);

		okHttpBuilder.certificatePinner(CertificatePinning.getCertificatePinner());
		okHttpBuilder.addInterceptor(new UserAgentInterceptor(DP3T.getUserAgent()));

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BuildConfig.STATS_URL)
				.client(okHttpBuilder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		statsService = retrofit.create(StatsService.class);
	}

	public StatsResponseModel getStats() throws IOException, ResponseError, SignatureException {

		Response<StatsResponseModel> statsResponse = statsService.getStats().execute();
		if (statsResponse.isSuccessful()) {
			return statsResponse.body();
		} else {
			throw new ResponseError(statsResponse.raw());
		}
	}

}
