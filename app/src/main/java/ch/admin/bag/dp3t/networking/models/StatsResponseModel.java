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

import java.text.DecimalFormat;
import java.util.List;

public class StatsResponseModel {

	private static double ONE_MILLION = 1000000;

	private int totalActiveUsers;
	private List<HistoryDataPointModel> history;

	public int getTotalActiveUsers() {
		return totalActiveUsers;
	}

	public String getTotalActiveUsersInMillions() {
		DecimalFormat df = new DecimalFormat("##.##");
		double usersInMillion = totalActiveUsers / ONE_MILLION;
		return df.format(usersInMillion);
	}

	public List<HistoryDataPointModel> getHistory() {
		return history;
	}

}
