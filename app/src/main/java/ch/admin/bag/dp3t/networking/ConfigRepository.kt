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
import android.os.Build
import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.networking.models.ConfigResponseModel
import ch.admin.bag.dp3t.storage.SecureStorage
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.backend.SignatureVerificationInterceptor
import org.dpppt.android.sdk.backend.UserAgentInterceptor
import org.dpppt.android.sdk.util.SignatureUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class ConfigRepository(context: Context) {

	companion object {
		private const val APP_VERSION_PREFIX_ANDROID = "android-"
		private const val OS_VERSION_PREFIX_ANDROID = "android"
	}

	private val configService: ConfigService
	private val secureStorage: SecureStorage


	init {
		val okHttpBuilder = OkHttpClient.Builder()

		val publicKey = SignatureUtil.getPublicKeyFromCertificateBase64OrThrow(BuildConfig.CONFIG_CERTIFICATE)
		okHttpBuilder.addInterceptor(SignatureVerificationInterceptor(publicKey))

		val cacheSize = 5 * 1024 * 1024 // 5 MB
		val cache = Cache(context.cacheDir, cacheSize.toLong())
		okHttpBuilder.cache(cache)

		okHttpBuilder.certificatePinner(CertificatePinning.getCertificatePinner())
		okHttpBuilder.addInterceptor(UserAgentInterceptor(DP3T.getUserAgent()))

		val retrofit = Retrofit.Builder()
			.baseUrl(BuildConfig.CONFIG_URL)
			.client(okHttpBuilder.build())
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		configService = retrofit.create(ConfigService::class.java)
		secureStorage = SecureStorage.getInstance(context)
	}

	@Throws(IOException::class, ResponseError::class)
	suspend fun getConfig(context: Context?): ConfigResponseModel {
		val appVersion = APP_VERSION_PREFIX_ANDROID + BuildConfig.VERSION_NAME
		val osVersion = OS_VERSION_PREFIX_ANDROID + Build.VERSION.SDK_INT
		val buildNumber = BuildConfig.BUILD_TIME.toString()
		val enModuleVersion = DP3T.getENModuleVersion(context).toString()
		val configResponse = configService.getConfig(appVersion, osVersion, buildNumber, enModuleVersion)
		if (configResponse.isSuccessful) {
			secureStorage.lastConfigLoadSuccess = System.currentTimeMillis()
			secureStorage.lastConfigLoadSuccessAppVersion = BuildConfig.VERSION_CODE
			secureStorage.lastConfigLoadSuccessSdkInt = Build.VERSION.SDK_INT
			return configResponse.body() ?: throw ResponseError(configResponse.raw())
		} else {
			throw ResponseError(configResponse.raw())
		}
	}
}