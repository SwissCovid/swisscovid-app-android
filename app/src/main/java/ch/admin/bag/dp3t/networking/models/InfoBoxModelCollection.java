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

class InfoBoxModelCollection {

	private InfoBoxModel deInfoBox;
	private InfoBoxModel frInfoBox;
	private InfoBoxModel itInfoBox;
	private InfoBoxModel enInfoBox;

	public InfoBoxModel getDeInfoBox() {
		return deInfoBox;
	}

	public InfoBoxModel getFrInfoBox() {
		return frInfoBox;
	}

	public InfoBoxModel getItInfoBox() {
		return itInfoBox;
	}

	public InfoBoxModel getEnInfoBox() {
		return enInfoBox;
	}

	public InfoBoxModel getInfoBox(String languageKey) {
		switch (languageKey) {
			case "de":
				return deInfoBox;
			case "en":
				return enInfoBox;
			case "fr":
				return frInfoBox;
			case "it":
				return itInfoBox;
		}
		return null;
	}

}
