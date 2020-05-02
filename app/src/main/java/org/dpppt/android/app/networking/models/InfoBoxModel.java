/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.networking.models;

public class InfoBoxModel {

	private String title;
	private String msg;
	private String url;
	private String urlTitle;

	public InfoBoxModel(String title, String msg, String url, String urlTitle) {
		this.title = title;
		this.msg = msg;
		this.url = url;
		this.urlTitle = urlTitle;
	}

	public String getTitle() {
		return title;
	}

	public String getMsg() {
		return msg;
	}

	public String getUrl() {
		return url;
	}

	public String getUrlTitle() {
		return urlTitle;
	}

}
