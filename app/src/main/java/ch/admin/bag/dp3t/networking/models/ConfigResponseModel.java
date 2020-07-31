/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.networking.models;

import java.util.List;

public class ConfigResponseModel {

	private boolean forceUpdate;
	private InfoBoxModelCollection infoBox;
	private SdkConfigModel androidGaenSdkConfig;
	private List<CountryModel> supportedCountries;

	public boolean getDoForceUpdate() {
		return forceUpdate;
	}

	public InfoBoxModelCollection getInfoBox() {
		return infoBox;
	}

	public InfoBoxModel getInfoBox(String languageKey) {
		if (infoBox == null) return null;
		return infoBox.getInfoBox(languageKey);
	}

	public SdkConfigModel getSdkConfig() {
		return androidGaenSdkConfig;
	}

	public List<CountryModel> getCountryModels() { return supportedCountries; }

}
