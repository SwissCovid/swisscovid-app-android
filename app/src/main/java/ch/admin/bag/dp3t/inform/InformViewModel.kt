package ch.admin.bag.dp3t.inform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import ch.admin.bag.dp3t.checkin.models.UserUploadPayload
import ch.admin.bag.dp3t.checkin.networking.UserUploadRepository
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.inform.models.Resource
import ch.admin.bag.dp3t.inform.models.SelectableCheckinItem
import ch.admin.bag.dp3t.util.toUploadVenueInfo
import kotlinx.coroutines.Dispatchers
import org.crowdnotifier.android.sdk.CrowdNotifier

private const val USER_UPLOAD_VERSION = 3

class InformViewModel(application: Application) : AndroidViewModel(application) {

	private val userUploadRepository = UserUploadRepository()
	private val diaryStorage = DiaryStorage.getInstance(application)

	val selectableDiaryItems = diaryStorage.entries.map { SelectableCheckinItem(it, isSelected = false) }

	fun userUpload() = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		try {
			emit(Resource.success(data = userUploadRepository.userUpload(getUserUploadPayload())))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
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

}