package ch.admin.bag.dp3t.inform

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
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
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val USER_UPLOAD_VERSION = 3
private const val TIMEOUT_VALID_CODE = 1000L * 60 * 5
private const val MAX_EXPOSURE_AGE_MILLIS = 10 * 24 * 60 * 60 * 1000L
private const val ISOLATION_DURATION_DAYS = 14L
private const val KEY_HAS_SHARED_DP3T_KEYS = "KEY_HAS_SHARED_DP3T_KEYS"

class InformViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {

	private val authCodeRepository = AuthCodeRepository(application)
	private val userUploadRepository = UserUploadRepository()
	private val diaryStorage = DiaryStorage.getInstance(application)
	private val secureStorage = SecureStorage.getInstance(application)

	var selectableCheckinItems = diaryStorage.entries.map { SelectableCheckinItem(it, isSelected = false) }
	var hasSharedDP3TKeys: Boolean
		get() = state.get<Boolean>(KEY_HAS_SHARED_DP3T_KEYS) ?: false
		set(value) = state.set(KEY_HAS_SHARED_DP3T_KEYS, value)

	fun userUpload() = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		try {
			val authorizationHeader = getAuthorizationHeader(secureStorage.lastCheckinInformToken)
			emit(Resource.success(data = userUploadRepository.userUpload(getUserUploadPayload(), authorizationHeader)))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, exception = exception))
		}
	}

	fun getLastCovidcode(): String? {
		val lastCovidcode = secureStorage.lastInformCode

		return if (System.currentTimeMillis() - secureStorage.lastInformRequestTime < TIMEOUT_VALID_CODE) {
			lastCovidcode
		} else {
			null
		}
	}

	fun authenticateCovidcode(covidcode: String) = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		try {
			emit(Resource.success(loadAccessTokens(covidcode)))
		} catch (exception: Throwable) {
			emit(Resource.error(data = null, exception = exception))
		}
	}

	fun informExposed(activity: Activity) = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		val authorizationHeader = getAuthorizationHeader(secureStorage.lastDP3TInformToken)
		val onsetDate = JwtUtil.getOnsetDate(secureStorage.lastDP3TInformToken)

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
						hasSharedDP3TKeys = true
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

	private suspend fun loadAccessTokens(covidcode: String) {
		val lastTimestamp = secureStorage.lastInformRequestTime
		val accessToken = secureStorage.lastDP3TInformToken
		val lastCovidcode = secureStorage.lastInformCode
		if (!(System.currentTimeMillis() - lastTimestamp < TIMEOUT_VALID_CODE && accessToken != null) || covidcode != lastCovidcode) {
			val accessTokens = authCodeRepository.getAccessToken(AuthenticationCodeRequestModel(covidcode, 0))
			secureStorage.saveInformTimeAndCodeAndToken(
				covidcode, accessTokens.dp3TAccessToken.accessToken, accessTokens.checkInAccessToken.accessToken
			)
		}
	}

	fun filterSelectableDiaryItems() {
		selectableCheckinItems = selectableCheckinItems.filter {
			Date(it.diaryEntry.departureTime).after(JwtUtil.getOnsetDate(secureStorage.lastCheckinInformToken))
		}
	}


	private fun getUserUploadPayload(): UserUploadPayload {
		val userUploadPayloadBuilder = UserUploadPayload.newBuilder().setVersion(USER_UPLOAD_VERSION)
		selectableCheckinItems.filter {
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