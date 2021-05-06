package ch.admin.bag.dp3t.checkin.utils;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ch.admin.bag.dp3t.R;

public class DateTimeUtils {

	public static String getHourMinuteTimeString(long timeStamp, String delimiter) {
		Calendar time = Calendar.getInstance();
		time.setTimeInMillis(timeStamp);
		return prependZero(time.get(Calendar.HOUR_OF_DAY)) + delimiter + prependZero(time.get(Calendar.MINUTE));
	}

	public static String getDaysAgoString(long timeStamp, Context context) {
		final long diff = getStartOfDay().getTime() - timeStamp;
		if (diff < 0) {
			return context.getResources().getString(R.string.date_today);
		} else {
			long daysAgo = TimeUnit.MILLISECONDS.toDays(diff) + 1;
			if (daysAgo == 1) {
				return context.getResources().getString(R.string.date_one_day_ago);
			} else {
				return context.getResources().getString(R.string.date_days_ago)
						.replace("{COUNT}", String.valueOf(daysAgo));
			}
		}
	}

	private static String prependZero(int timeUnit) {
		if (timeUnit < 10) {
			return "0" + timeUnit;
		} else {
			return String.valueOf(timeUnit);
		}
	}

	private static Date getStartOfDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

}
