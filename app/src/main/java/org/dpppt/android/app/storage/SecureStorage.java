package org.dpppt.android.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecureStorage {

	private static final String PREFERENCES = "SecureStorage";

	private static final String KEY_INFECTED_DATE = "infected_date";
	private static final String KEY_INFORM_TIME_REQ = "inform_time_req";
	private static final String KEY_INFORM_CODE_REQ = "inform_code_req";
	private static final String KEY_INFORM_TOKEN_REQ = "inform_token_req";
	private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
	private static final String KEY_LAST_SHOWN_CONTACT_ID = "last_shown_contact_id";
	private static final String KEY_HOTLINE_CALL_PENDING = "hotline_call_pending";
	private static final String KEY_HOTLINE_EVER_CALLED = "hotline_ever_called";
	private static final String KEY_PENDING_REPORTS_HEADER_ANIMATION = "pending_reports_header_animation";

	private static SecureStorage instance;

	private SharedPreferences prefs;

	private SecureStorage(@NonNull Context context) {

		try {
			String masterKeys = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
			this.prefs = EncryptedSharedPreferences
					.create(PREFERENCES, masterKeys, context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
							EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
		} catch (GeneralSecurityException | IOException e) {
			this.prefs = null;
			e.printStackTrace();
		}
	}

	public static SecureStorage getInstance(Context context) {
		if (instance == null) {
			instance = new SecureStorage(context);
		}
		return instance;
	}

	public long getInfectedDate() {
		return prefs.getLong(KEY_INFECTED_DATE, 0);
	}

	public void setInfectedDate(long date) {
		prefs.edit().putLong(KEY_INFECTED_DATE, date).apply();
	}

	public void saveInformTimeAndCodeAndToken(String informCode, String informToken) {
		prefs.edit().putLong(KEY_INFORM_TIME_REQ, System.currentTimeMillis())
				.putString(KEY_INFORM_CODE_REQ, informCode)
				.putString(KEY_INFORM_TOKEN_REQ, informToken)
				.apply();
	}

	public void clearInformTimeAndCodeAndToken() {
		prefs.edit().remove(KEY_INFORM_TIME_REQ)
				.remove(KEY_INFORM_CODE_REQ)
				.remove(KEY_INFORM_TOKEN_REQ)
				.apply();
	}

	public long getLastInformRequestTime() {
		return prefs.getLong(KEY_INFORM_TIME_REQ, 0);
	}

	public String getLastInformCode() {
		return prefs.getString(KEY_INFORM_CODE_REQ, null);
	}

	public String getLastInformToken() {
		return prefs.getString(KEY_INFORM_TOKEN_REQ, null);
	}

	public boolean getOnboardingCompleted() {
		return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
	}

	public void setOnboardingCompleted(boolean completed) {
		prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
	}

	public int getLastShownContactId() {
		return prefs.getInt(KEY_LAST_SHOWN_CONTACT_ID, -1);
	}

	public void setLastShownContactId(int contactId) {
		prefs.edit().putInt(KEY_LAST_SHOWN_CONTACT_ID, contactId).apply();
	}

	public boolean isHotlineCallPending() {
		return prefs.getBoolean(KEY_HOTLINE_CALL_PENDING, false);
	}

	public void setHotlineCallPending(boolean pending) {
		prefs.edit().putBoolean(KEY_HOTLINE_CALL_PENDING, pending).apply();
	}

	public boolean wasHotlineEverCalled() {
		return prefs.getBoolean(KEY_HOTLINE_EVER_CALLED, false);
	}

	public void justCalledHotline() {
		prefs.edit().putBoolean(KEY_HOTLINE_CALL_PENDING, false)
				.putBoolean(KEY_HOTLINE_EVER_CALLED, true)
				.apply();
	}

	public boolean isReportsHeaderAnimationPending() {
		return prefs.getBoolean(KEY_PENDING_REPORTS_HEADER_ANIMATION, false);
	}

	public void setReportsHeaderAnimationPending(boolean pending) {
		prefs.edit().putBoolean(KEY_PENDING_REPORTS_HEADER_ANIMATION, pending).apply();
	}

}