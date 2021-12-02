/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.networking.models

data class VaccinationBookingInfoModel(
	val title: String,
	val text: String,
	val info: String,
	val impfcheckTitle: String?,
	val impfcheckText: String?,
	val impfcheckButton: String?,
	val impfcheckUrl: String?
)
