package ch.admin.bag.dp3t.checkin.generateqrcode

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.*
import ch.admin.bag.dp3t.checkin.models.GeneratedQrCodesWrapper
import ch.admin.bag.dp3t.checkin.models.SwissCovidLocationData
import ch.admin.bag.dp3t.checkin.models.VenueType
import ch.admin.bag.dp3t.checkin.utils.toQrCodePayload
import ch.admin.bag.dp3t.checkin.utils.toVenueInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.model.v3.QRCodePayload
import org.crowdnotifier.android.sdk.utils.Base64Util
import java.io.IOException

private const val MASTER_PUBLIC_KEY_BASE64 =
	"TqRYigTM6YVO7/UJQuu3199mRqj0cSTp4DXCFlxbz9UqDLrASr07C9HJVWYtl08V7xGEGSSXWbQSRfRt/bqqDK0HQQGnZ/VWZxToo88txtgQ1ij7pYJwaBHAGGndLICL"
private const val ONE_MINUTE_IN_MILLIS = 60 * 1000L
private const val ONE_HOUR_IN_MILLIS = 60 * ONE_MINUTE_IN_MILLIS
private const val AUTOMATIC_CHECKOUT_DELAY_MS = 12 * ONE_HOUR_IN_MILLIS
private const val CHECKOUT_WARNING_DELAY_MS = 8 * ONE_HOUR_IN_MILLIS
private const val SWISSCOVID_LOCATION_DATA_VERSION = 1
private const val QR_CODE_VALIDITY_DURATION_MS = 10 * 365 * 24 * ONE_HOUR_IN_MILLIS // 10 years
private val REMINDER_DELAY_OPTIONS_MS = listOf(30, 60, 120, 240).map { it * ONE_HOUR_IN_MILLIS } // 30, 60, 120 and 240 minutes

private val Context.generatedQrCodesDataStore: DataStore<GeneratedQrCodesWrapper> by dataStore(
	fileName = "generatedQrCodes.pb",
	serializer = GeneratedQrCodesSerializer
)

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {


	private val generatedQrCodesFlow = getApplication<Application>().generatedQrCodesDataStore.data.buffer(1).catch { exception ->
		if (exception is IOException) {
			Log.e("QRCodeViewModel", "Error reading generated QR Codes.", exception)
		} else {
			throw exception
		}
	}

	val generatedQrCodesLiveData = generatedQrCodesFlow.map { wrapper ->
		wrapper.generatedQrCodesList.map { it.toVenueInfo() }
	}.asLiveData()


	fun generateAndSaveQrCode(description: String, venueType: VenueType) = viewModelScope.launch {

		val swissCovidLocationData = SwissCovidLocationData.newBuilder()
			.setVersion(SWISSCOVID_LOCATION_DATA_VERSION)
			.setAutomaticCheckoutDelaylMs(AUTOMATIC_CHECKOUT_DELAY_MS)
			.setCheckoutWarningDelayMs(CHECKOUT_WARNING_DELAY_MS)
			.addAllReminderDelayOptionsMs(REMINDER_DELAY_OPTIONS_MS)
			.setTypeValue(venueType.number)
			.build()

		val generatedVenueInfo = CrowdNotifier.generateVenueInfo(
			description,
			"",
			swissCovidLocationData.toByteArray(),
			System.currentTimeMillis() / 1000,
			(System.currentTimeMillis() + QR_CODE_VALIDITY_DURATION_MS) / 1000,
			Base64.decode(MASTER_PUBLIC_KEY_BASE64, Base64.DEFAULT)
		)


		val newWrapper = GeneratedQrCodesWrapper.newBuilder()
			.addAllGeneratedQrCodes(generatedQrCodesFlow.first().generatedQrCodesList)
			.addGeneratedQrCodes(generatedVenueInfo.toQrCodePayload())
			.build()
		saveGeneratedVenueInfo(newWrapper)

	}

	private suspend fun saveGeneratedVenueInfo(generatedQrCodesWrapper: GeneratedQrCodesWrapper) {

		try {
			getApplication<Application>().generatedQrCodesDataStore.updateData { generatedQrCodesWrapper }
		} catch (e: Exception) {
			//TODO: Handle Error
		}

	}

}

sealed class QrCodePayloadState {
	data class SUCCESS(val qrCodePayload: QRCodePayload) : QrCodePayloadState()
	data class ERROR(val exception: java.lang.Exception) : QrCodePayloadState()
}
