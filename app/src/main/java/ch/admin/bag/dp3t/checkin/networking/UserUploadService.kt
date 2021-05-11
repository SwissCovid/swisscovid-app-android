package ch.admin.bag.dp3t.checkin.networking

import ch.admin.bag.dp3t.checkin.models.UserUploadPayload
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface UserUploadService {

	@POST("v3/userupload")
	suspend fun userUpload(
		@Body userUploadPayload: UserUploadPayload, @Header("Authorization") authorizationHeader: String?
	)

}