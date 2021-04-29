package ch.admin.bag.dp3t.checkin.models;

import android.content.Context;
import androidx.annotation.IdRes;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;

public enum ReminderOption {

	OFF(0, R.id.check_in_fragment_toggle_button_1),
	THIRTY_MINUTES(BuildConfig.FLAVOR.equals("dev") ? 1 : 30, R.id.check_in_fragment_toggle_button_2),
	ONE_HOUR(60, R.id.check_in_fragment_toggle_button_3),
	TWO_HOURS(120, R.id.check_in_fragment_toggle_button_4),
	FOUR_HOURS(240, R.id.check_in_fragment_toggle_button_5);

	private static final long MINUTE_IN_MILLIS = 60 * 1000L;

	private int delayMinutes;
	@IdRes private int toggleButtonId;
	private String name;

	ReminderOption(int delayMinutes, @IdRes int toggleButtonId) {
		this.delayMinutes = delayMinutes;
		this.toggleButtonId = toggleButtonId;
		this.name = name;
	}

	public int getToggleButtonId() {
		return toggleButtonId;
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

	public static ReminderOption getReminderOptionForToggleButtonId(@IdRes int toggleButtonId) {
		switch (toggleButtonId) {
			case R.id.check_in_fragment_toggle_button_2:
				return THIRTY_MINUTES;
			case R.id.check_in_fragment_toggle_button_3:
				return ONE_HOUR;
			case R.id.check_in_fragment_toggle_button_4:
				return TWO_HOURS;
			default:
				return OFF;
		}
	}
}
