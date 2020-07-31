package ch.admin.bag.dp3t.travel;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import ch.admin.bag.dp3t.R;

public class Country {

	private String isoCode;
	private boolean isFavourite;
	private boolean isActive;
	private long deactivationTimestamp;

	public Country(String isoCode, boolean isFavourite, boolean isActive, long deactivationTimestamp) {
		this.isoCode = isoCode;
		this.isFavourite = isFavourite;
		this.isActive = isActive;
		this.deactivationTimestamp = deactivationTimestamp;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public boolean isFavourite() {
		return isFavourite;
	}

	public void setFavourite(boolean favourite) {
		isFavourite = favourite;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public long getDeactivationTimestamp() {
		return deactivationTimestamp;
	}

	public void setDeactivationTimestamp(long deactivationTimestamp) {
		this.deactivationTimestamp = deactivationTimestamp;
	}

	public String getCountryName(Context context) {
		return (new Locale("", getIsoCode())).getDisplayCountry(new Locale(context.getString(R.string.language_key)));
	}

	public int getFlagResId() {
		//TODO: Add Flag resources (PP-602)
		return -1;
	}

	public String getStatusText(Context context, int daysToKeepNotificationsActive) {
		long factorDayMillis = 24 * 60 * 60 * 1000;
		String replaceStringDate = "{DATE}";
		String statusText = "";
		if (!isActive() &&
				getDeactivationTimestamp() > System.currentTimeMillis() - daysToKeepNotificationsActive * factorDayMillis) {

			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));

			long activeUntil = getDeactivationTimestamp() + daysToKeepNotificationsActive * factorDayMillis;

			statusText = context.getResources().getString(R.string.travel_screen_notifications_activated_until)
					.replace(replaceStringDate, sdf.format(activeUntil));
		}
		return statusText;
	}

}
