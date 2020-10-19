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

public class FaqEntryModel {

	private String title;
	private String text;
	private String iconAndroid;

	/* optional */
	private String linkTitle;
	private String linkUrl;

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	public String getIconAndroid() {
		return iconAndroid;
	}

	public String getLinkTitle() {
		return linkTitle;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

}