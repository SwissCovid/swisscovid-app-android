package ch.admin.bag.dp3t.util

import ch.admin.bag.dp3t.checkin.models.UploadVenueInfo
import com.google.protobuf.ByteString
import org.crowdnotifier.android.sdk.model.UserUploadInfo

fun UserUploadInfo.toUploadVenueInfo(): UploadVenueInfo {
	return UploadVenueInfo.newBuilder()
		.setPreId(ByteString.copyFrom(preId))
		.setTimeKey(ByteString.copyFrom(timeKey))
		.setNotificationKey(ByteString.copyFrom(notificationKey))
		.setIntervalStartMs(intervalStartMs)
		.setIntervalEndMs(intervalEndMs)
		.setFake(false)
		.build()
}