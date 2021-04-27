package ch.admin.bag.dp3t.viewmodel.model;

import android.content.Context;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;


public enum ReminderOption {

	OFF(0),
	THIRTY_MINUTES(BuildConfig.FLAVOR.equals("dev") ? 1 : 30),
	ONE_HOUR(60),
	TWO_HOURS(120),
	FOUR_HOURS(240);

	private static final long MINUTE_IN_MILLIS = 60 * 1000L;

	private int delayMinutes;
	private String name;

	ReminderOption(int delayMinutes) {
		this.delayMinutes = delayMinutes;
		this.name = name;
	}

	public int getDelayMinutes() {
		return delayMinutes;
	}

	public long getDelayMillis() {
		return delayMinutes * MINUTE_IN_MILLIS;
	}

	public int getDelayHours() {
		return delayMinutes / 60;
	}

	public String getName(Context context) {
		switch (this) {
			case THIRTY_MINUTES:
				return context.getString(R.string.reminder_option_minutes).replace("{MINUTES}", String.valueOf(getDelayMinutes()));
			case OFF:
				return context.getString(R.string.reminder_option_off);
			default:
				return context.getString(R.string.reminder_option_hours).replace("{HOURS}", String.valueOf(getDelayHours()));
		}
	}
}
