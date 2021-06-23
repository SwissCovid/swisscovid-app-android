package ch.admin.bag.dp3t.networking.models

import androidx.annotation.Keep

class AuthenticationCodeRequestModel(@field:Keep private val authorizationCode: String, @field:Keep private val fake: Int)

data class AuthenticationCodeResponseModelV2(
	val checkInAccessToken: AuthenticationCodeResponseModel,
	val dp3TAccessToken: AuthenticationCodeResponseModel
)

class AuthenticationCodeResponseModel(val accessToken: String)


data class OnsetResponse(val onset: String)
