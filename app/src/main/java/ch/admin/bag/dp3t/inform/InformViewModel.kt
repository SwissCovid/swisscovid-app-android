package ch.admin.bag.dp3t.inform

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import ch.admin.bag.dp3t.checkin.models.UserUploadPayload
import ch.admin.bag.dp3t.checkin.networking.UserUploadRepository
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.inform.models.Resource
import ch.admin.bag.dp3t.inform.models.SelectableCheckinItem
import ch.admin.bag.dp3t.networking.AuthCodeRepository
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.JwtUtil
import ch.admin.bag.dp3t.util.toUploadVenueInfo
import kotlinx.coroutines.Dispatchers
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.backend.ResponseCallback
import org.dpppt.android.sdk.models.DayDate
import org.dpppt.android.sdk.models.ExposeeAuthMethodAuthorization
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val USER_UPLOAD_VERSION = 3
private const val TIMEOUT_VALID_CODE = 1000L * 60 * 5
private const val MAX_EXPOSURE_AGE_MILLIS = 10 * 24 * 60 * 60 * 1000L
private const val ISOLATION_DURATION_DAYS = 14L

class InformViewModel(application: Application) : AndroidViewModel(application) {

	private val authCodeRepository = AuthCodeRepository(application)
	private val userUploadRepository = UserUploadRepository()
	private val diaryStorage = DiaryStorage.getInstance(application)
	private val secureStorage = SecureStorage.getInstance(application)

	//TODO: Persist these Items for DKA cases
	val selectableDiaryItems = diaryStorage.entries.map { SelectableCheckinItem(it, isSelected = false) }

	fun userUpload() = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		try {
			emit(Resource.success(data = userUploadRepository.userUpload(getUserUploadPayload())))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, exception = exception))
		}
	}

	fun getLastAuthCode(): String? {
		val lastCode = secureStorage.lastInformCode
		val lastToken = secureStorage.lastDP3TInformToken

		if (System.currentTimeMillis() - secureStorage.lastInformRequestTime < TIMEOUT_VALID_CODE) {
			return lastCode
		} else if (lastCode != null || lastToken != null) {
			secureStorage.clearInformTimeAndCodeAndToken()
		}
		return null
	}

	fun authenticateInputAndGetDP3TAccessToken(authCode: String) = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		val lastTimestamp = secureStorage.lastInformRequestTime
		val lastDP3TAuthToken = secureStorage.lastDP3TInformToken
		if (System.currentTimeMillis() - lastTimestamp < TIMEOUT_VALID_CODE && lastDP3TAuthToken != null) {
			emit(Resource.success(lastDP3TAuthToken))
		} else {
			try {
				val accessTokens = authCodeRepository.getAccessToken(AuthenticationCodeRequestModel(authCode, 0))
				secureStorage.saveInformTimeAndCodeAndToken(
					authCode,
					accessTokens.dp3TAccessToken.accessToken,
					accessTokens.checkInAccessToken.accessToken
				)
				emit(Resource.success(data = accessTokens.dp3TAccessToken.accessToken))
			} catch (exception: Exception) {
				emit(Resource.error(data = null, exception = exception))
			}
		}
	}

	fun authenticateInputAndGetCheckinAccessToken(authCode: String) = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		val lastTimestamp = secureStorage.lastInformRequestTime
		val lastCheckinAuthToken = secureStorage.lastCheckinInformToken
		if (System.currentTimeMillis() - lastTimestamp < TIMEOUT_VALID_CODE && lastCheckinAuthToken != null) {
			emit(Resource.success(lastCheckinAuthToken))
		} else {
			try {
				val accessTokens = authCodeRepository.getAccessToken(AuthenticationCodeRequestModel(authCode, 0))
				secureStorage.saveInformTimeAndCodeAndToken(
					authCode,
					accessTokens.dp3TAccessToken.accessToken,
					accessTokens.checkInAccessToken.accessToken
				)
				emit(Resource.success(data = accessTokens.checkInAccessToken.accessToken))
			} catch (exception: Exception) {
				emit(Resource.error(data = null, exception = exception))
			}
		}
	}

	fun informExposed(accessToken: String, activity: Activity) = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		val authorizationHeader = getAuthorizationHeader(accessToken)
		val onsetDate = JwtUtil.getOnsetDate(accessToken)

		var result: Resource<Unit>? = null

		// Wrapping traditional callback in a suspendCoroutine
		suspendCoroutine<Unit> { continuation ->
			DP3T.sendIAmInfected(activity, onsetDate, ExposeeAuthMethodAuthorization(authorizationHeader),
				object : ResponseCallback<DayDate> {
					override fun onSuccess(oldestSharedKeyDayDate: DayDate) {

						// Store the oldest shared Key date of this report (but at least now-MAX_EXPOSURE_AGE_MILLIS)
						val oldestSharedKeyDate = Math.max(
							oldestSharedKeyDayDate.startOfDayTimestamp,
							System.currentTimeMillis() - MAX_EXPOSURE_AGE_MILLIS
						)
						secureStorage.positiveReportOldestSharedKey = oldestSharedKeyDate

						// Ask if user wants to end isolation after 14 days
						val isolationEndDialogTimestamp =
							System.currentTimeMillis() + TimeUnit.DAYS.toMillis(ISOLATION_DURATION_DAYS)
						secureStorage.isolationEndDialogTimestamp = isolationEndDialogTimestamp
						result = Resource.success(Unit)
						continuation.resume(Unit)
					}

					override fun onError(throwable: Throwable) {
						result = Resource.error(null, throwable)
						continuation.resume(Unit)
					}
				})
		}
		result?.let { emit(it) }
	}

	private fun getUserUploadPayload(): UserUploadPayload {
		val userUploadPayloadBuilder = UserUploadPayload.newBuilder().setVersion(USER_UPLOAD_VERSION)
		selectableDiaryItems.filter {
			it.isSelected
		}.map {
			CrowdNotifier.generateUserUploadInfo(it.diaryEntry.venueInfo, it.diaryEntry.arrivalTime, it.diaryEntry.departureTime)
		}.flatten().forEach {
			userUploadPayloadBuilder.addVenueInfos(it.toUploadVenueInfo())
		}
		return userUploadPayloadBuilder.build()
	}

	private fun getAuthorizationHeader(accessToken: String): String {
		return "Bearer $accessToken"
	}

}