package org.dpppt.android.app.util;

import java.util.concurrent.TimeUnit;

public class DateUtils {

	public static int getDaysDiff(long date) {

		try {
			return (int) TimeUnit.DAYS.convert(System.currentTimeMillis() - date, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}
