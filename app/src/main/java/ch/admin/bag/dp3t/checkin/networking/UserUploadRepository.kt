package ch.admin.bag.dp3t.checkin.networking

import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.checkin.models.UserUploadPayload
import okhttp3.OkHttpClient
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.backend.UserAgentInterceptor
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory

class UserUploadRepository {

	private var userUploadService: UserUploadService

	init {
		val okHttpBuilder = OkHttpClient.Builder()

		okHttpBuilder.addInterceptor(UserAgentInterceptor(DP3T.getUserAgent()))

		val retrofit = Retrofit.Builder()
			.baseUrl(BuildConfig.PUBLISHED_CROWDNOTIFIER_KEYS_BASE_URL)
			.client(okHttpBuilder.build())
			.addConverterFactory(ProtoConverterFactory.create())
			.build()
		userUploadService = retrofit.create(UserUploadService::class.java)
	}

	suspend fun userUpload(userUploadPayload: UserUploadPayload) = userUploadService.userUpload(userUploadPayload)


}