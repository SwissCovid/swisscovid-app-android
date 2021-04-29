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

import java.util.HashMap;

public class InfoBoxModelCollection extends HashMap<String, InfoBoxModel> {

	private static final String KEY_POSTFIX = "InfoBox";

	public InfoBoxModel getInfoBox(String languageKey) {
		return get(getKeyForLang(languageKey));
	}

	private static String getKeyForLang(String language) {
		return language + KEY_POSTFIX;
	}

}
