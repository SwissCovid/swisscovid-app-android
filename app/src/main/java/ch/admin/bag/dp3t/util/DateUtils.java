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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.dpppt.android.sdk.models.DayDate;

public class DateUtils {

	private static final DateFormat DATE_TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
	private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance();

	public static int getDaysDiff(long timestamp) {
		try {
			return (int) TimeUnit.DAYS.convert(
					getLocalStartOfDayTimestamp(System.currentTimeMillis()) - getLocalStartOfDayTimestamp(timestamp),
					TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private static long getLocalStartOfDayTimestamp(long timestamp) {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTime(new Date(timestamp));
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime().getTime();
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

	public static String getFormattedDateWrittenMonth(long date) {
		return getFormattedDateWrittenMonth(date, TimeZone.getDefault());
	}

	public static String getFormattedDateWrittenMonth(long date, TimeZone timezone) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy");
		sdf.setTimeZone(timezone);
		return sdf.format(new Date(date));
	}

	public static Date getParsedDateStats(String date) {
		if (date == null) {
			return null;
		}
		try {
			// Needs to be inlined (rather than static) because SimpleDateFormat is not thread-safe
			final DateFormat DATE_PARSE_STATS = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

			return DATE_PARSE_STATS.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getFormattedDateStats(Date parsedDate) {
		// Fix the date format, rather than localising it.
		// This is an easy workaround for removing the year from the formatted date.
		final DateFormat DATE_FORMAT_STATS = new SimpleDateFormat("dd.MM.", Locale.ENGLISH);

		if (parsedDate != null) {
			return DATE_FORMAT_STATS.format(parsedDate);
		} else {
			return null;
		}
	}

}
