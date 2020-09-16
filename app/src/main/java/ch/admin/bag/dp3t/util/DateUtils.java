/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.dpppt.android.sdk.models.DayDate;

public class DateUtils {

	private static final DateFormat DATE_TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
	private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance();
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat DATE_LAST_UPDATED_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static int getDaysDiff(long date) {
		try {
			return (int) TimeUnit.DAYS.convert(System.currentTimeMillis() - date, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static int getDaysDiffUntil(DayDate from, DayDate to) {
		try {
			return (int) TimeUnit.DAYS.convert(to.getStartOfDayTimestamp() - from.getStartOfDayTimestamp(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static String getFormattedDateTime(long date) {
		return DATE_TIME_FORMAT.format(new Date(date));
	}

	public static String getFormattedDate(long date) {
		return DATE_FORMAT.format(new Date(date));
	}

	public static String getFormattedLastUpdatedDate(String date) {
		try {
			Date parsedDate = DATE_LAST_UPDATED_FORMAT.parse(date);
			if (parsedDate != null) {
				return DATE_FORMAT.format(parsedDate);
			} else {
				return null;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

}
