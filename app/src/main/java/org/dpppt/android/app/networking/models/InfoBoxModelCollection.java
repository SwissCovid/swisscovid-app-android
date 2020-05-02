/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.networking.models;

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
