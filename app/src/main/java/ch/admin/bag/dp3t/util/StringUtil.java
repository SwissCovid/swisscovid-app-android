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

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ch.admin.bag.dp3t.R;

public class StringUtil {

	private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);

	/**
	 * Creates a spannable where the {@code boldString} is set to bold within the {@code fullString}.
	 * Be aware that this only applies to the first occurence.
	 * @param fullString The entire string
	 * @param boldString The partial string to be made bold
	 * @return A partially bold spannable
	 */
	public static Spannable makePartiallyBold(@NonNull String fullString, @NonNull String boldString) {
		int start = fullString.indexOf(boldString);
		if (start >= 0) {
			return makePartiallyBold(fullString, start, start + boldString.length());
		}
		return new SpannableString(fullString);
	}

	public static SpannableString makePartiallyBold(@NonNull String string, int start, int end) {
		SpannableString result = new SpannableString(string);
		result.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return result;
	}

	public static String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0)
			return String.format("%0" + paddingLength + "d", 0) + hex;
		else
			return hex;
	}

	public static String getHourMinuteTimeString(long timeStamp, String delimiter) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(timeStamp);
		return prependZero(time.get(Calendar.HOUR_OF_DAY)) + delimiter + prependZero(time.get(Calendar.MINUTE));
	}

	private static String prependZero(int timeUnit) {
		if (timeUnit < 10) {
			return "0" + timeUnit;
		} else {
			return String.valueOf(timeUnit);
		}
	}

	/**
	 * Formats a duration in milliseconds to a String of hours, minutes and seconds, or to only hours and minutes if the
	 * duration is more than 10 hours
	 * @param duration in milliseconds
	 * @return a formatted duration String
	 */
	public static String getShortDurationString(long duration) {
		if (duration >= TimeUnit.HOURS.toMillis(10)) {
			return String.format(Locale.GERMAN, "%d:%02d",
					TimeUnit.MILLISECONDS.toHours(duration),
					TimeUnit.MILLISECONDS.toMinutes(duration - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(duration)))
			);
		} else {
			return getDurationString(duration);
		}
	}

	public static String getDurationString(long duration) {
		if (duration >= ONE_HOUR) {
			return String.format(Locale.GERMAN, "%d:%02d:%02d",
					TimeUnit.MILLISECONDS.toHours(duration),
					TimeUnit.MILLISECONDS.toMinutes(duration - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(duration))),
					TimeUnit.MILLISECONDS.toSeconds(duration - TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(duration)))
			);
		} else {
			return String.format(Locale.GERMAN, "%02d:%02d",
					TimeUnit.MILLISECONDS.toMinutes(duration),
					TimeUnit.MILLISECONDS.toSeconds(duration - TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(duration)))
			);
		}
	}

	public static String getCheckOutDateString(Context context, long checkInTime, long checkOutTime) {
		DateFormat dateFormat = new SimpleDateFormat("EEEE, dd. MMMM");
		String checkInDate = dateFormat.format(new Date(checkInTime));
		String checkOutDate = dateFormat.format(new Date(checkOutTime));
		if (checkInDate.equals(checkOutDate)) {
			return checkInDate;
		} else {
			return context.getResources().getString(R.string.checkout_from_to_date).replace("{DATE1}", checkInDate)
					.replace("{DATE2}", "\n" + checkOutDate);
		}
	}

	public static String getDaysAgoString(long timeStamp, Context context) {
		final long daysAgo = DateUtils.getDaysDiff(timeStamp);
		if (daysAgo <= 0) {
			return context.getResources().getString(R.string.date_today);
		} else if (daysAgo == 1) {
			return context.getResources().getString(R.string.date_one_day_ago);
		} else {
			return context.getResources().getString(R.string.date_days_ago)
					.replace("{COUNT}", String.valueOf(daysAgo));
		}
	}

	public static String getReportDateString(long timestamp, boolean withDiff, boolean withPrefix, Context context) {
		if (!withDiff) {
			return DateUtils.getFormattedDateWrittenMonth(timestamp);
		}
		String dateStr;
		if (withPrefix) {
			dateStr = context.getString(R.string.date_text_before_date).replace("{DATE}", DateUtils.getFormattedDate(timestamp));
		} else {
			dateStr = DateUtils.getFormattedDate(timestamp);
		}
		dateStr += " / ";
		int daysDiff = DateUtils.getDaysDiff(timestamp);

		if (daysDiff == 0) {
			dateStr += context.getString(R.string.date_today);
		} else if (daysDiff == 1) {
			dateStr += context.getString(R.string.date_one_day_ago);
		} else {
			dateStr += context.getString(R.string.date_days_ago).replace("{COUNT}", String.valueOf(daysDiff));
		}
		return dateStr;
	}

}
