package ch.admin.bag.dp3t.networking.models

data class AuthenticationCodeResponseModelV2(
	val checkInAccessToken: AuthenticationCodeResponseModel,
	val dp3TAccessToken: AuthenticationCodeResponseModel
)