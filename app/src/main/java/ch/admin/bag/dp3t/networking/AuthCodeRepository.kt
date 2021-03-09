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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.networking.errors.InvalidCodeError
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeResponseModel
import kotlinx.coroutines.*
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.backend.ResponseCallback
import org.dpppt.android.sdk.backend.UserAgentInterceptor
import org.dpppt.android.sdk.internal.logger.Logger
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

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

	fun getAccessToken(
		authenticationCode: AuthenticationCodeRequestModel,
		callbackListener: ResponseCallback<AuthenticationCodeResponseModel>,
		lifecycleOwner: LifecycleOwner
	) {
		lifecycleOwner.lifecycleScope.launch {
			withContext(Dispatchers.IO) {
				try {
					val response = authCodeService.getAccessToken(authenticationCode)
					if (response.isSuccessful) {
						withContext(Dispatchers.Main) {
							callbackListener.onSuccess(response.body())
						}
					} else {
						withContext(Dispatchers.Main) {
							if (response.code() == 404) {
								callbackListener.onError(InvalidCodeError())
							} else {
								callbackListener.onError(ResponseError(response.raw()))
							}
						}
					}
				} catch (e: Exception) {
					withContext(Dispatchers.Main) {
						Logger.e(TAG, "getAccessToken", e)
						callbackListener.onError(e)
					}
				}
			}
		}
	}

	@Throws(IOException::class, ResponseError::class)
	suspend fun getAccessTokenSync(authenticationCode: AuthenticationCodeRequestModel): AuthenticationCodeResponseModel =
		withContext(Dispatchers.IO) {
			val response = authCodeService.getAccessToken(authenticationCode)
			if (!response.isSuccessful) throw ResponseError(response.raw())
			return@withContext response.body() ?: throw ResponseError(response.raw())
		}
}