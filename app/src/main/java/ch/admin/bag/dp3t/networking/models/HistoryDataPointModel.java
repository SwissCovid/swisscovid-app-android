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

import ch.admin.bag.dp3t.util.DateUtils;

public class HistoryDataPointModel {

	private String date;
	private Integer newInfections;
	private Integer newInfectionsSevenDayAverage;
	private Integer covidcodesEntered;

	public String getDateRaw() {
		return date;
	}

	public Date getDateParsed() {
		return DateUtils.getParsedDateStats(date);
	}

	public String getDateFormatted() {
		return DateUtils.getFormattedDateStats(getDateParsed());
	}

	public Integer getNewInfections() {
		return newInfections;
	}

	public Integer getNewInfectionsSevenDayAverage() {
		return newInfectionsSevenDayAverage;
	}

	public Integer getCovidcodesEntered() {
		return covidcodesEntered;
	}

}
