package org.dpppt.android.app.networking.models;

public class ConfigResponseModel {

	private boolean forceUpdate;

	private InfoBoxModel infobox = null;

	public ConfigResponseModel(boolean forceUpdate, InfoBoxModel infoBox) {
		this.forceUpdate = forceUpdate;
		this.infobox = infobox;
	}

	public boolean getDoForceUpdate() {
		return forceUpdate;
	}

	public InfoBoxModel getInfoBox() {
		return infobox;
	}

}
