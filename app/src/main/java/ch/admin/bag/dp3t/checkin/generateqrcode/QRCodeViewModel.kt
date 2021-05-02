package ch.admin.bag.dp3t.checkin.generateqrcode

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.crowdnotifier.android.sdk.model.v3.ProtoV3
import java.io.IOException

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {

	private val qrCodeStateMutableLiveData: MutableLiveData<QrCodePayloadState> = MutableLiveData()
	val qrCodeStateLiveData: LiveData<QrCodePayloadState> = qrCodeStateMutableLiveData

	private val qrCodeListStateMutableLiveData: MutableLiveData<List<ProtoV3.QRCodePayload>> = MutableLiveData()
	val qrCodeListStateLiveData: LiveData<List<ProtoV3.QRCodePayload>> = qrCodeListStateMutableLiveData

	fun saveQRCodePayload(qrCodePayload: ProtoV3.QRCodePayload) {
		viewModelScope.launch {
			try {
				val qrCodePayload = getApplication<Application>().qrCodePayloadDataStore.updateData {
					qrCodePayload
				}
				qrCodeStateMutableLiveData.postValue(QrCodePayloadState.SUCCESS(qrCodePayload))
			} catch (e: Exception) {
				qrCodeStateMutableLiveData.postValue(QrCodePayloadState.ERROR(e))
			}

		}
	}

	fun loadQrCodePayloads() {
		viewModelScope.launch {
			val flow: Flow<ProtoV3.QRCodePayload> = getApplication<Application>().qrCodePayloadDataStore.data.catch { exception ->
				if (exception is IOException) {
					Log.e("QRCODEPayload", "Error reading sort order preferences.", exception)
					emit(ProtoV3.QRCodePayload.getDefaultInstance())
				} else {
					throw exception
				}
			}

			val arrayList = arrayListOf<ProtoV3.QRCodePayload>()
			flow.collect { payload ->
				arrayList.add(payload)
				qrCodeListStateMutableLiveData.postValue(arrayList.toList())
			}

		}
	}
}

sealed class QrCodePayloadState {
	data class SUCCESS(val qrCodePayload: ProtoV3.QRCodePayload) : QrCodePayloadState()
	data class ERROR(val exception: java.lang.Exception) : QrCodePayloadState()
}
