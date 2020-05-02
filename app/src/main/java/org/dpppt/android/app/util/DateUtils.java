/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

	private static final DateFormat DATE_TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
	private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance();

	public static int getDaysDiff(long date) {

		try {
			return (int) TimeUnit.DAYS.convert(System.currentTimeMillis() - date, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static int getDaysDiffUntil(long date, int addDays) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		calendar.add(Calendar.DATE, addDays);
		date = calendar.getTimeInMillis();

		try {
			return (int) TimeUnit.DAYS.convert(date - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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

}
