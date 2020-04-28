package org.dpppt.android.app.networking.models;

public class ConfigResponseModel {

	private boolean forceUpdate;

	private InfoBoxModel infoBox = null;

	public ConfigResponseModel(boolean forceUpdate, InfoBoxModel infobox) {
		this.forceUpdate = forceUpdate;
		this.infoBox = infobox;
	}

	public boolean getDoForceUpdate() {
		return forceUpdate;
	}

	public InfoBoxModel getInfoBox() {
		return infoBox;
	}

}
