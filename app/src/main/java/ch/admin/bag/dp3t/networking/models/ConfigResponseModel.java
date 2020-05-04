/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package ch.admin.bag.dp3t.networking.models;

public class ConfigResponseModel {

	private boolean forceUpdate;
	private boolean forceTraceShutdown;
	private InfoBoxModelCollection infoBox;
	private SdkConfigModel sdkConfig;

	public boolean getDoForceUpdate() {
		return forceUpdate;
	}

	public boolean getForceTraceShutdown() {
		return forceTraceShutdown;
	}

	public InfoBoxModelCollection getInfoBox() {
		return infoBox;
	}

	public InfoBoxModel getInfoBox(String languageKey) {
		return infoBox.getInfoBox(languageKey);
	}

	public SdkConfigModel getSdkConfig() {
		return sdkConfig;
	}

}
