package ch.admin.bag.dp3t.checkin.generateqrcode

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.*
import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.checkin.models.GeneratedQrCodesWrapper
import ch.admin.bag.dp3t.checkin.models.SwissCovidLocationData
import ch.admin.bag.dp3t.checkin.models.VenueType
import ch.admin.bag.dp3t.checkin.utils.SingleLiveEvent
import ch.admin.bag.dp3t.extensions.toQrCodePayload
import ch.admin.bag.dp3t.extensions.toVenueInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.model.VenueInfo
import org.crowdnotifier.android.sdk.utils.Base64Util
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max


private const val ONE_MINUTE_IN_MILLIS = 60 * 1000L
private const val ONE_HOUR_IN_MILLIS = 60 * ONE_MINUTE_IN_MILLIS
private const val AUTOMATIC_CHECKOUT_DELAY_MS = 12 * ONE_HOUR_IN_MILLIS
private const val CHECKOUT_WARNING_DELAY_MS = 8 * ONE_HOUR_IN_MILLIS
private const val SWISSCOVID_LOCATION_DATA_VERSION = 4
private const val QR_CODE_VALIDITY_DURATION_MS = 100000 * 24 * ONE_HOUR_IN_MILLIS // 100'000 days
private val REMINDER_DELAY_OPTIONS_MS =
	listOf(if (BuildConfig.IS_FLAVOR_DEV) 1 else 30, 60, 120, 240).map { it * ONE_MINUTE_IN_MILLIS } // 30, 60, 120 and 240 minutes
private const val MAX_QR_CODE_PIXEL_SIZE = 1000
const val QR_CODE_PDF_FILE_NAME = "swisscovid-qr-code.pdf"

private val Context.generatedQrCodesDataStore: DataStore<GeneratedQrCodesWrapper> by dataStore(
	fileName = "generatedQrCodes.pb",
	serializer = GeneratedQrCodesSerializer
)

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {

	private val generatedQrCodesFlow = application.generatedQrCodesDataStore.data.buffer(1).catch { exception ->
		if (exception is IOException) {
			Log.e("QRCodeViewModel", "Error reading generated QR Codes.", exception)
		} else {
			throw exception
		}
	}

	private val pdfDirectory = File(application.externalCacheDir, "pdfs").apply { if (!exists()) mkdirs() }

	val generatedQrCodesLiveData = generatedQrCodesFlow.map { wrapper ->
		wrapper.generatedQrCodesList.map { it.toVenueInfo() }
	}.asLiveData()

	val selectedQrCodeBitmap = SingleLiveEvent<Bitmap>()
	val selectedQrCodePdf = SingleLiveEvent<File>()

	fun deleteQrCode(venueInfo: VenueInfo) = viewModelScope.launch {
		val builder = GeneratedQrCodesWrapper.newBuilder()
		generatedQrCodesFlow.first().generatedQrCodesList.forEach {
			if (venueInfo.toQrCodePayload() != it) {
				builder.addGeneratedQrCodes(it)
			}
		}
		saveGeneratedQrCode(builder.build())
	}

	fun generateAndSaveQrCode(description: String) = liveData(Dispatchers.IO) {

		val swissCovidLocationData = SwissCovidLocationData.newBuilder()
			.setVersion(SWISSCOVID_LOCATION_DATA_VERSION)
			.setAutomaticCheckoutDelaylMs(AUTOMATIC_CHECKOUT_DELAY_MS)
			.setCheckoutWarningDelayMs(CHECKOUT_WARNING_DELAY_MS)
			.addAllReminderDelayOptionsMs(REMINDER_DELAY_OPTIONS_MS)
			.setType(VenueType.USER_QR_CODE)
			.build()

		val generatedVenueInfo = CrowdNotifier.generateVenueInfo(
			description,
			"",
			swissCovidLocationData.toByteArray(),
			System.currentTimeMillis() / 1000,
			(System.currentTimeMillis() + QR_CODE_VALIDITY_DURATION_MS) / 1000,
			Base64Util.fromBase64(BuildConfig.QR_MASTER_PUBLIC_KEY_BASE_64)
		)


		val newWrapper = GeneratedQrCodesWrapper.newBuilder()
			.addAllGeneratedQrCodes(generatedQrCodesFlow.first().generatedQrCodesList)
			.addGeneratedQrCodes(generatedVenueInfo.toQrCodePayload())
			.build()
		saveGeneratedQrCode(newWrapper)
		emit(generatedVenueInfo)
	}

	fun generateQrCodeBitmapAndPdf(venueInfo: VenueInfo, qrCodeSize: Int) = viewModelScope.launch(Dispatchers.IO) {
		launch(Dispatchers.IO) {
			selectedQrCodeBitmap.postValue(
				QrCode.create(venueInfo.toQrCodeString("https://" + BuildConfig.ENTRY_QR_CODE_HOST))
					.renderToBitmap(max(qrCodeSize, MAX_QR_CODE_PIXEL_SIZE))
			)
		}
		launch(Dispatchers.IO) {
			val document = createEntryPdf(venueInfo, getApplication())

			val file = File(pdfDirectory, QR_CODE_PDF_FILE_NAME)

			FileOutputStream(file).use {
				document.writeTo(it)
				document.close()
			}

			selectedQrCodePdf.postValue(file)
		}
	}

	private suspend fun saveGeneratedQrCode(generatedQrCodesWrapper: GeneratedQrCodesWrapper) {
		try {
			getApplication<Application>().generatedQrCodesDataStore.updateData { generatedQrCodesWrapper }
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}

}
