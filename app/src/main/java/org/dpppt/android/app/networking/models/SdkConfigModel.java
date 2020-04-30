package org.dpppt.android.app.networking.models;

public class SdkConfigModel {

	private int numberOfWindowsForExposure;
	private float eventThreshold;
	private float badAttenuationThreshold;
	private float contactAttenuationThreshold;

	public int getNumberOfWindowsForExposure() {
		return numberOfWindowsForExposure;
	}

	public float getEventThreshold() {
		return eventThreshold;
	}

	public float getBadAttenuationThreshold() {
		return badAttenuationThreshold;
	}

	public float getContactAttenuationThreshold() {
		return contactAttenuationThreshold;
	}

}