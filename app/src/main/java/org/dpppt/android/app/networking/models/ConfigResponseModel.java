package org.dpppt.android.app.networking.models;

public class ConfigResponseModel {

	private boolean forceUpdate;
	private InfoBoxModelCollection infoBox;
	private SdkConfigModel sdkConfig;

	public boolean getDoForceUpdate() {
		return forceUpdate;
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
