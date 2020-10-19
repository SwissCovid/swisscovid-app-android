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

public class WhatToDoPositiveTestTextsModel {

	private String enterCovidcodeBoxSupertitle;
	private String enterCovidcodeBoxTitle;
	private String enterCovidcodeBoxText;
	private String enterCovidcodeBoxButtonTitle;

	//dismissible will be ignored by clients
	private InfoBoxModel infoBox;

	private List<FaqEntryModel> faqEntries;

	public String getEnterCovidcodeBoxSupertitle() {
		return enterCovidcodeBoxSupertitle;
	}

	public String getEnterCovidcodeBoxTitle() {
		return enterCovidcodeBoxTitle;
	}

	public String getEnterCovidcodeBoxText() {
		return enterCovidcodeBoxText;
	}

	public String getEnterCovidcodeBoxButtonTitle() {
		return enterCovidcodeBoxButtonTitle;
	}

	public InfoBoxModel getInfoBox() {
		return infoBox;
	}

	public List<FaqEntryModel> getFaqEntries() {
		return faqEntries;
	}

}
