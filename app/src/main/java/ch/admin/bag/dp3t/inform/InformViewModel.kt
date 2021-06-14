package ch.admin.bag.dp3t.inform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.liveData
import ch.admin.bag.dp3t.checkin.models.UploadVenueInfo
import ch.admin.bag.dp3t.checkin.models.VenueType
import ch.admin.bag.dp3t.checkin.networking.UserUploadRepository
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.extensions.getSwissCovidLocationData
import ch.admin.bag.dp3t.inform.models.Resource
import ch.admin.bag.dp3t.inform.models.SelectableCheckinItem
import ch.admin.bag.dp3t.networking.AuthCodeRepository
import ch.admin.bag.dp3t.networking.errors.InvalidCodeError
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.JwtUtil
import ch.admin.bag.dp3t.util.toUploadVenueInfo
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.DP3TKotlin
import org.dpppt.android.sdk.PendingUploadTask
import org.dpppt.android.sdk.backend.ResponseCallback
import org.dpppt.android.sdk.internal.AppConfigManager
import org.dpppt.android.sdk.models.DayDate
import org.dpppt.android.sdk.models.ExposeeAuthMethodAuthorization
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

private const val TIMEOUT_VALID_CODE = 1000L * 60 * 5
private const val MAX_EXPOSURE_AGE_MILLIS = 10 * 24 * 60 * 60 * 1000L
private const val ISOLATION_DURATION_DAYS = 14L
private const val UPLOAD_REQUEST_TIME_PADDING = 5000L
private const val CHECKOUT_TIME_PADDING_MS = 30 * 60 * 1000L
private const val KEY_COVIDCODE = "KEY_COVIDCODE"
private const val KEY_HAS_SHARED_DP3T_KEYS = "KEY_HAS_SHARED_DP3T_KEYS"
private const val KEY_HAS_SHARED_CHECKINS = "KEY_HAS_SHARED_CHECKINS"
private const val KEY_PENDING_UPLOAD_TASK = "KEY_PENDING_UPLOAD_TASK"
private const val KEY_SELECTED_DIARY_ENTRIES = "KEY_SELECTED_DIARY_ENTRIES"
private const val KEY_ONSET_DATE = "KEY_ONSET_DATE"
private const val KEY_ONSET_REQUEST_TIME = "KEY_ONSET_REQUEST_TIME"

class InformViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {

	private val authCodeRepository = AuthCodeRepository(application)
	private val userUploadRepository = UserUploadRepository()
	private val diaryStorage = DiaryStorage.getInstance(application)
	private val secureStorage = SecureStorage.getInstance(application)

	private var selectedDiaryEntryIds: LongArray
		get() = state.get<LongArray>(KEY_SELECTED_DIARY_ENTRIES) ?: longArrayOf()
		set(value) = state.set(KEY_SELECTED_DIARY_ENTRIES, value)

	var covidCode: String
		get() = state.get<String>(KEY_COVIDCODE) ?: getLastCovidcode()
		set(value) = state.set(KEY_COVIDCODE, value)

	var hasSharedDP3TKeys: Boolean
		get() = state.get<Boolean>(KEY_HAS_SHARED_DP3T_KEYS) ?: false
		set(value) = state.set(KEY_HAS_SHARED_DP3T_KEYS, value)

	var hasSharedCheckins: Boolean
		get() = state.get<Boolean>(KEY_HAS_SHARED_CHECKINS) ?: false
		set(value) = state.set(KEY_HAS_SHARED_CHECKINS, value)

	var pendingUploadTask: PendingUploadTask?
		get() = state.get<PendingUploadTask?>(KEY_PENDING_UPLOAD_TASK)
		set(value) = state.set(KEY_PENDING_UPLOAD_TASK, value)

	private var onsetDate: Long?
		get() = state.get<Long>(KEY_ONSET_DATE)
		set(value) = state.set(KEY_ONSET_DATE, value)

	private var onsetResponseTime: Long?
		get() = state.get<Long>(KEY_ONSET_REQUEST_TIME)
		set(value) = state.set(KEY_ONSET_REQUEST_TIME, value)

	fun loadOnsetDate() = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		try {
			loadOnsetDate(covidCode)
			emit(Resource.success(data = null))
			onsetResponseTime = System.currentTimeMillis()
		} catch (exception: Throwable) {
			when (exception) {
				is InvalidCodeError -> emit(Resource.error(InformRequestError.BLACK_INVALID_AUTH_RESPONSE_FORM, exception))
				is ResponseError -> emit(Resource.error(InformRequestError.BLACK_STATUS_ERROR, exception))
				else -> emit(Resource.error(InformRequestError.BLACK_MISC_NETWORK_ERROR, exception))
			}
		}
	}

	fun performUpload() = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		val onsetResponseTime = onsetResponseTime ?: System.currentTimeMillis()
		val timeBetweenOnsetAndUploadRequest = (System.currentTimeMillis() - onsetResponseTime).toInt()
		delay(UPLOAD_REQUEST_TIME_PADDING - (System.currentTimeMillis() - onsetResponseTime) % UPLOAD_REQUEST_TIME_PADDING)
		var oldestSharedKey: Long? = null
		var oldestSharedCheckin: Long? = null
		try {
			loadAccessTokens(covidCode)
		} catch (exception: Throwable) {
			when (exception) {
				is InvalidCodeError -> emit(Resource.error(InformRequestError.BLACK_INVALID_AUTH_RESPONSE_FORM, exception))
				is ResponseError -> emit(Resource.error(InformRequestError.BLACK_STATUS_ERROR, exception))
				else -> emit(Resource.error(InformRequestError.BLACK_MISC_NETWORK_ERROR, exception))
			}
			return@liveData
		}
		try {
			if (hasSharedDP3TKeys) {
				oldestSharedKey = uploadTEKs()
			} else {
				performFakeTEKUpload()
			}
		} catch (exception: Throwable) {
			when (exception) {
				is ResponseError -> emit(Resource.error(InformRequestError.RED_STATUS_ERROR, exception))
				is CancellationException -> emit(Resource.error(InformRequestError.RED_USER_CANCELLED_SHARE, exception))
				is ApiException -> emit(Resource.error(InformRequestError.RED_EXPOSURE_API_ERROR, exception))
				else -> emit(Resource.error(InformRequestError.RED_MISC_NETWORK_ERROR, exception))
			}
			return@liveData
		}

		try {
			if (hasSharedCheckins) {
				oldestSharedCheckin = performCheckinsUpload(timeBetweenOnsetAndUploadRequest)
			} else {
				performFakeCheckinsUpload(timeBetweenOnsetAndUploadRequest)
			}
		} catch (exception: Throwable) {
			when (exception) {
				is HttpException -> emit(Resource.error(InformRequestError.USER_UPLOAD_NETWORK_ERROR, exception))
				else -> emit(Resource.error(InformRequestError.USER_UPLOAD_UNKONWN_ERROR, exception))
			}
			return@liveData
		}

		val appConfigManager = AppConfigManager.getInstance(getApplication())
		appConfigManager.iAmInfected = true
		appConfigManager.iAmInfectedIsResettable = true
		DP3T.stop(getApplication())
		secureStorage.positiveReportOldestSharedKeyOrCheckin = listOfNotNull(oldestSharedKey, oldestSharedCheckin).minOrNull() ?: -1
		emit(Resource.success(data = null))
	}

	fun getSelectableCheckinItems(): List<SelectableCheckinItem> {
		return diaryStorage.entries.filter {
			it.venueInfo.getSwissCovidLocationData().type == VenueType.USER_QR_CODE && it.departureTime >= onsetDate ?: 0
					&& it.departureTime > System.currentTimeMillis() - MAX_EXPOSURE_AGE_MILLIS
		}.map {
			SelectableCheckinItem(it, isSelected = selectedDiaryEntryIds.contains(it.id))
		}
	}

	fun setDiaryItemSelected(itemId: Long, isSelected: Boolean) {
		selectedDiaryEntryIds = selectedDiaryEntryIds.toMutableList().apply {
			if (isSelected) add(itemId) else remove(itemId)
		}.distinct().toLongArray()
	}

	private fun getLastCovidcode(): String {
		val lastCovidcode = secureStorage.lastInformCode

		return if (System.currentTimeMillis() - secureStorage.lastInformRequestTime < TIMEOUT_VALID_CODE) {
			lastCovidcode ?: ""
		} else {
			""
		}
	}

	private suspend fun performFakeTEKUpload() {
		val authorizationHeader = ExposeeAuthMethodAuthorization(getAuthorizationHeader(secureStorage.lastDP3TInformToken))
		DP3TKotlin.sendFakeInfectedRequest(getApplication(), authorizationHeader)
	}

	/**
	 * Returns the oldestSharedKeyDate
	 */
	private suspend fun uploadTEKs(): Long {
		val authorizationHeader = getAuthorizationHeader(secureStorage.lastDP3TInformToken)
		val onsetDate = JwtUtil.getOnsetDate(secureStorage.lastDP3TInformToken)

		// Wrapping traditional callback in a suspendCoroutine
		return suspendCoroutine { continuation ->
			pendingUploadTask?.performUpload(getApplication(), onsetDate, ExposeeAuthMethodAuthorization(authorizationHeader),
				object : ResponseCallback<DayDate> {
					override fun onSuccess(oldestSharedKeyDayDate: DayDate) {

						// Store the oldest shared Key date of this report (but at least now-MAX_EXPOSURE_AGE_MILLIS)
						val oldestSharedKeyDate = max(
							oldestSharedKeyDayDate.startOfDayTimestamp,
							System.currentTimeMillis() - MAX_EXPOSURE_AGE_MILLIS
						)
						secureStorage.positiveReportOldestSharedKey = oldestSharedKeyDate

						// Ask if user wants to end isolation after 14 days
						val isolationEndDialogTimestamp =
							System.currentTimeMillis() + TimeUnit.DAYS.toMillis(ISOLATION_DURATION_DAYS)
						secureStorage.isolationEndDialogTimestamp = isolationEndDialogTimestamp
						hasSharedDP3TKeys = true
						continuation.resume(oldestSharedKeyDate)
					}

					override fun onError(throwable: Throwable) {
						continuation.resumeWithException(throwable)
					}
				})
		}
	}

	/**
	 * Returns oldest shared Checkin
	 */
	private suspend fun performCheckinsUpload(timeBetweenOnsetAndUploadRequest: Int): Long? {
		val authorizationHeader = getAuthorizationHeader(secureStorage.lastCheckinInformToken)
		val uploadVenueInfos = getUploadVenueInfos()
		userUploadRepository.userUpload(uploadVenueInfos, timeBetweenOnsetAndUploadRequest, authorizationHeader)
		return uploadVenueInfos.minOfOrNull { it.intervalStartMs }
	}

	private suspend fun performFakeCheckinsUpload(timeBetweenOnsetAndUploadRequest: Int) {
		val authorizationHeader = getAuthorizationHeader(secureStorage.lastCheckinInformToken)
		userUploadRepository.fakeUserUpload(timeBetweenOnsetAndUploadRequest, authorizationHeader)
	}

	private suspend fun loadOnsetDate(covidcode: String) {
		val onsetResponse = authCodeRepository.getOnsetDate(AuthenticationCodeRequestModel(covidcode, 0))
		onsetDate = SimpleDateFormat("yyyy-MM-dd").parse(onsetResponse.onset)?.time
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

	private fun getUploadVenueInfos(): List<UploadVenueInfo> {
		return getSelectableCheckinItems().filter {
			it.isSelected
		}.map {
			CrowdNotifier.generateUserUploadInfo(
				it.diaryEntry.venueInfo, it.diaryEntry.arrivalTime, it.diaryEntry.departureTime + CHECKOUT_TIME_PADDING_MS
			)
		}.flatten().map {
			it.toUploadVenueInfo()
		}
	}

	private fun getAuthorizationHeader(accessToken: String): String {
		return "Bearer $accessToken"
	}

}