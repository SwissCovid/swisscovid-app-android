/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.stats;

import android.os.Parcel;
import android.os.Parcelable;

public class StatsDetailsSection implements Parcelable {
	private final String sectionTitle;
	private final String sectionContent;

	public StatsDetailsSection(String sectionTitle, String sectionContent) {
		this.sectionTitle = sectionTitle;
		this.sectionContent = sectionContent;
	}

	protected StatsDetailsSection(Parcel in) {
		sectionTitle = in.readString();
		sectionContent = in.readString();
	}

	public static final Creator<StatsDetailsSection> CREATOR = new Creator<StatsDetailsSection>() {
		@Override
		public StatsDetailsSection createFromParcel(Parcel in) {
			return new StatsDetailsSection(in);
		}

		@Override
		public StatsDetailsSection[] newArray(int size) {
			return new StatsDetailsSection[size];
		}
	};

	public String getSectionTitle() {
		return sectionTitle;
	}

	public String getSectionContent() {
		return sectionContent;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(sectionTitle);
		dest.writeString(sectionContent);
	}

}
