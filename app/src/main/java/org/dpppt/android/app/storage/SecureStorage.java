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

	private static SecureStorage instance;

	private final SharedPreferences prefs;

	private SecureStorage(@NonNull SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public static SecureStorage getInstance(@NonNull Context context) {
		if (instance == null) {
			try {
				String masterKeys = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
				SharedPreferences prefs = EncryptedSharedPreferences
						.create(PREFERENCES, masterKeys, context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
								EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
				instance = new SecureStorage(prefs);
			} catch (GeneralSecurityException | IOException e) {
				e.printStackTrace();
			}
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
		prefs.edit()
				.putLong(KEY_INFORM_TIME_REQ, System.currentTimeMillis())
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
}