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

public class HistoryDataPointModel {

	private String date;
	private int newInfections;
	private int newInfectionsSevenDayAverage;
	private int covidcodesEntered;

	public String getDate() {
		return date;
	}

	public int getNewInfections() {
		return newInfections;
	}

	public int getNewInfectionsSevenDayAverage() {
		return newInfectionsSevenDayAverage;
	}

	public int getCovidcodesEntered() {
		return covidcodesEntered;
	}

}
