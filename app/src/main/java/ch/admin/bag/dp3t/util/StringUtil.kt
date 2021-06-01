/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.util

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import ch.admin.bag.dp3t.R
import java.math.BigInteger
import java.util.*
import java.util.concurrent.TimeUnit

object StringUtil {
	private val ONE_HOUR = TimeUnit.HOURS.toMillis(1)

	/**
	 * Creates a spannable where the `boldString` is set to bold within the `fullString`.
	 * Be aware that this only applies to the first occurence.
	 * @param fullString The entire string
	 * @param boldString The partial string to be made bold
	 * @return A partially bold spannable
	 */
	fun makePartiallyBold(fullString: String, boldString: String): Spannable {
		val start = fullString.indexOf(boldString)
		return if (start >= 0) {
			makePartiallyBold(fullString, start, start + boldString.length)
		} else SpannableString(fullString)
	}

	@JvmStatic
	fun makePartiallyBold(string: String, start: Int, end: Int): SpannableString {
		val result = SpannableString(string)
		result.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
		return result
	}

	@JvmStatic
	fun toHex(array: ByteArray): String {
		val bi = BigInteger(1, array)
		val hex = bi.toString(16)
		val paddingLength = array.size * 2 - hex.length
		return if (paddingLength > 0) String.format("%0" + paddingLength + "d", 0) + hex else hex
	}

	@JvmStatic
	fun getHourMinuteTimeString(timeStamp: Long, delimiter: String): String {
		val time = Calendar.getInstance()
		time.timeInMillis = timeStamp
		return prependZero(time[Calendar.HOUR_OF_DAY]) + delimiter + prependZero(time[Calendar.MINUTE])
	}

	private fun prependZero(timeUnit: Int): String {
		return if (timeUnit < 10) {
			"0$timeUnit"
		} else {
			timeUnit.toString()
		}
	}

	/**
	 * Formats a duration in milliseconds to a String of hours and minutes with units. e.g. "1 h 12 min"
	 * duration is more than 10 hours
	 * @param duration in milliseconds
	 * @return a formatted duration String
	 */
	fun getShortDurationStringWithUnits(duration: Long, context: Context): String {
		return if (duration < ONE_HOUR) {
			val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
			minutes.toMinutesString(context)
		} else {
			val hours = TimeUnit.MILLISECONDS.toHours(duration)
			val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours)
			if (minutes > 0L) {
				"${hours.toHoursString(context)} ${minutes.toMinutesString(context)}"
			} else {
				hours.toHoursString(context)
			}
		}
	}

	private fun Long.toHoursString(context: Context): String {
		return context.getString(R.string.reminder_option_hours).replace("{HOURS}", this.toString())
	}

	private fun Long.toMinutesString(context: Context): String {
		return context.getString(R.string.reminder_option_minutes).replace("{MINUTES}", this.toString())
	}

	/**
	 * Formats a duration in milliseconds to a String of hours, minutes and seconds, or to only hours and minutes if the
	 * duration is more than 10 hours
	 * @param duration in milliseconds
	 * @return a formatted duration String
	 */
	@JvmStatic
	fun getShortDurationString(duration: Long): String {
		return if (duration >= TimeUnit.HOURS.toMillis(10)) {
			String.format(
				Locale.GERMAN, "%d:%02d",
				TimeUnit.MILLISECONDS.toHours(duration),
				TimeUnit.MILLISECONDS
					.toMinutes(duration - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(duration)))
			)
		} else {
			getDurationString(duration)
		}
	}

	fun getDurationString(duration: Long): String {
		return if (duration >= ONE_HOUR) {
			String.format(
				Locale.GERMAN, "%d:%02d:%02d",
				TimeUnit.MILLISECONDS.toHours(duration),
				TimeUnit.MILLISECONDS.toMinutes(
					duration - TimeUnit.HOURS.toMillis(
						TimeUnit.MILLISECONDS.toHours(
							duration
						)
					)
				),
				TimeUnit.MILLISECONDS.toSeconds(
					duration - TimeUnit.MINUTES.toMillis(
						TimeUnit.MILLISECONDS.toMinutes(
							duration
						)
					)
				)
			)
		} else {
			String.format(
				Locale.GERMAN, "%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(duration),
				TimeUnit.MILLISECONDS.toSeconds(
					duration - TimeUnit.MINUTES.toMillis(
						TimeUnit.MILLISECONDS.toMinutes(
							duration
						)
					)
				)
			)
		}
	}

	fun getDaysAgoString(timeStamp: Long, context: Context): String {
		val daysAgo = DateUtils.getDaysDiff(timeStamp).toLong()
		return if (daysAgo <= 0) {
			context.resources.getString(R.string.date_today)
		} else if (daysAgo == 1L) {
			context.resources.getString(R.string.date_one_day_ago)
		} else {
			context.resources.getString(R.string.date_days_ago)
				.replace("{COUNT}", daysAgo.toString())
		}
	}

	fun getReportDateString(timestamp: Long, withDiff: Boolean, withPrefix: Boolean, context: Context): String {
		if (!withDiff) {
			return DateUtils.getFormattedDateWrittenMonth(timestamp)
		}
		var dateStr: String
		dateStr = if (withPrefix) {
			context.getString(R.string.date_text_before_date)
				.replace("{DATE}", DateUtils.getFormattedDate(timestamp))
		} else {
			DateUtils.getFormattedDate(timestamp)
		}
		dateStr += " / "
		val daysDiff = DateUtils.getDaysDiff(timestamp)
		dateStr += if (daysDiff == 0) {
			context.getString(R.string.date_today)
		} else if (daysDiff == 1) {
			context.getString(R.string.date_one_day_ago)
		} else {
			context.getString(R.string.date_days_ago).replace("{COUNT}", daysDiff.toString())
		}
		return dateStr
	}
}