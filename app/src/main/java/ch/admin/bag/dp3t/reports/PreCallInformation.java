/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.reports;

public class PreCallInformation {

	private final String exposureDate;
	private final String code;

	public PreCallInformation(String exposureDate, String code) {
		this.exposureDate = exposureDate;
		this.code = code;
	}

	public String getExposureDate() {
		return exposureDate;
	}

	public String getCode() {
		return code;
	}

}
