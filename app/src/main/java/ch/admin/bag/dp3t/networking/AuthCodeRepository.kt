/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.networking

import android.content.Context
import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.networking.errors.InvalidCodeError
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeResponseModelV2
import ch.admin.bag.dp3t.networking.models.OnsetResponse
import kotlinx.coroutines.*
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.backend.UserAgentInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthCodeRepository(context: Context) {

	companion object {
		private const val TAG = "AuthCodeRepo"
	}

	private val authCodeService: AuthCodeService

	init {
		val okHttpBuilder = OkHttpClient.Builder()
		okHttpBuilder.addInterceptor { chain: Interceptor.Chain ->
			val request = chain.request()
				.newBuilder()
				.build()
			chain.proceed(request)
		}

		val cacheSize = 5 * 1024 * 1024 // 5 MB
		val cache = Cache(context.cacheDir, cacheSize.toLong())
		okHttpBuilder.cache(cache)

		okHttpBuilder.certificatePinner(CertificatePinning.getCertificatePinner())
		okHttpBuilder.addInterceptor(UserAgentInterceptor(DP3T.getUserAgent()))

		val retrofit = Retrofit.Builder()
			.baseUrl(BuildConfig.AUTH_CODE_URL)
			.client(okHttpBuilder.build())
			.addConverterFactory(GsonConverterFactory.create())
			.build()
		authCodeService = retrofit.create(AuthCodeService::class.java)
	}

	suspend fun getAccessToken(authCode: AuthenticationCodeRequestModel): AuthenticationCodeResponseModelV2 =
		withContext(Dispatchers.IO) {
			val response = authCodeService.getAccessTokenV2(authCode)
			if (!response.isSuccessful) {
				if (response.code() == 404) {
					throw InvalidCodeError()
				} else {
					throw ResponseError(response.raw())
				}
			}
			return@withContext response.body() ?: throw ResponseError(response.raw())
		}

	suspend fun getOnsetDate(authCode: AuthenticationCodeRequestModel): OnsetResponse {
		val response = authCodeService.getOnsetDate(authCode)
		if (!response.isSuccessful) {
			if (response.code() == 404) {
				throw InvalidCodeError()
			} else {
				throw ResponseError(response.raw())
			}
		}
		return response.body() ?: throw ResponseError(response.raw())
	}
}