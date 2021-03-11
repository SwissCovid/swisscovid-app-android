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

import java.util.Date;
import java.util.List;

import ch.admin.bag.dp3t.util.DateUtils;

public class StatsResponseModel {

	private String lastUpdated;
	private Integer totalActiveUsers;
	private Integer totalCovidcodesEntered;
	private Double covidcodesEntered0to2dPrevWeek;
	private Integer newInfectionsSevenDayAvg;
	private Double newInfectionsSevenDayAvgRelPrevWeek;
	private List<HistoryDataPointModel> history;

	public String getLastUpdatedRaw() {
		return lastUpdated;
	}

	public Date getLastUpdatedParsed() {
		return DateUtils.getParsedDateStats(lastUpdated);
	}

	public String getLastUpdatedFormatted() {
		return DateUtils.getFormattedDateStats(getLastUpdatedParsed());
	}

	public Integer getTotalActiveUsers() {
		return totalActiveUsers;
	}

	public Integer getTotalCovidcodesEntered() {
		return totalCovidcodesEntered;
	}

	public Double getCovidcodesEntered0to2dPrevWeek() {
		return covidcodesEntered0to2dPrevWeek;
	}

	public Integer getNewInfectionsSevenDayAvg() {
		return newInfectionsSevenDayAvg;
	}

	public Double getNewInfectionsSevenDayAvgRelPrevWeek() {
		return newInfectionsSevenDayAvgRelPrevWeek;
	}

	public List<HistoryDataPointModel> getHistory() {
		return history;
	}

}
