package ch.admin.bag.dp3t.checkin.networking

import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.checkin.models.UploadVenueInfo
import ch.admin.bag.dp3t.checkin.models.UserUploadPayload
import com.google.protobuf.ByteString
import okhttp3.OkHttpClient
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.backend.UserAgentInterceptor
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import java.util.*

private const val USER_UPLOAD_SIZE = 1024
private const val USER_UPLOAD_VERSION = 3

class UserUploadRepository {

	private var userUploadService: UserUploadService
	private val random = Random()

	init {
		val okHttpBuilder = OkHttpClient.Builder()

		okHttpBuilder.addInterceptor(UserAgentInterceptor(DP3T.getUserAgent()))

		val retrofit = Retrofit.Builder()
			.baseUrl(BuildConfig.REPORT_URL)
			.client(okHttpBuilder.build())
			.addConverterFactory(ProtoConverterFactory.create())
			.build()
		userUploadService = retrofit.create(UserUploadService::class.java)
	}

	suspend fun userUpload(
		uploadVenueInfos: List<UploadVenueInfo>,
		timeBetweenOnsetAndUploadRequest: Int,
		authorizationHeader: String
	) =
		userUploadService.userUpload(getUserUploadPayload(uploadVenueInfos, timeBetweenOnsetAndUploadRequest), authorizationHeader)

	suspend fun fakeUserUpload(timeBetweenOnsetAndUploadRequest: Int, authorizationHeader: String) =
		userUploadService.userUpload(getUserUploadPayload(listOf(), timeBetweenOnsetAndUploadRequest), authorizationHeader)

	private fun getUserUploadPayload(
		uploadVenueInfos: List<UploadVenueInfo>,
		timeBetweenOnsetAndUploadRequest: Int
	): UserUploadPayload {
		val userUploadPayloadBuilder = UserUploadPayload.newBuilder()
			.setVersion(USER_UPLOAD_VERSION)
			.setUserInteractionDurationMs(timeBetweenOnsetAndUploadRequest)
		userUploadPayloadBuilder.addAllVenueInfos(uploadVenueInfos)
		for (i in userUploadPayloadBuilder.venueInfosCount until USER_UPLOAD_SIZE) {
			userUploadPayloadBuilder.addVenueInfos(getRandomFakeVenueInfo())
		}
		return userUploadPayloadBuilder.build()
	}

	private fun getRandomFakeVenueInfo(): UploadVenueInfo {
		return UploadVenueInfo.newBuilder()
			.setPreId(ByteString.copyFrom(getRandomByteArray(32)))
			.setTimeKey(ByteString.copyFrom(getRandomByteArray(32)))
			.setNotificationKey(ByteString.copyFrom(getRandomByteArray(32)))
			.setIntervalStartMs(random.nextLong())
			.setIntervalEndMs(random.nextLong())
			.setFake(true)
			.build()
	}

	private fun getRandomByteArray(size: Int): ByteArray {
		return ByteArray(size).apply {
			random.nextBytes(this)
		}
	}
}