/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.networking

import ch.admin.bag.dp3t.networking.models.AuthenticationCodeRequestModel
import ch.admin.bag.dp3t.networking.models.AuthenticationCodeResponseModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthCodeService {
	@Headers("accept: */*", "content-type: application/json")
	@POST("v1/onset")
	suspend fun getAccessToken(@Body code: AuthenticationCodeRequestModel): Response<AuthenticationCodeResponseModel>
}