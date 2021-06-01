/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.storage;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.admin.bag.dp3t.networking.models.InfoBoxModelCollection;
import ch.admin.bag.dp3t.networking.models.TestLocationModel;
import ch.admin.bag.dp3t.networking.models.WhatToDoPositiveTestTextsCollection;
import ch.admin.bag.dp3t.networking.models.WhatToDoPositiveTestTextsModel;
import ch.admin.bag.dp3t.checkin.models.CheckInState;

public class SecureStorage {

	private static final String TAG = "SecureStorage";

	private static final String PREFERENCES = "SecureStorage";
	private static final String DEFAULT_TEST_LOCATIONS_JSON_PATH = "testlocations.json";

	private static final String KEY_INFECTED_DATE = "infected_date";
	private static final String KEY_INFORM_TIME_REQ = "inform_time_req";
	private static final String KEY_INFORM_CODE_REQ = "inform_code_req";
	private static final String KEY_INFORM_TOKEN_REQ = "inform_token_req";
	private static final String KEY_INFORM_TOKEN_CHECKIN_REQ = "inform_token_chekin_req";
	private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
	private static final String KEY_UPDATE_BOARDING_VERSION = "update_boarding_version";
	private static final String KEY_LAST_SHOWN_CONTACT_ID = "last_shown_contact_id";

	//KEY_LEITFADEN_OPEN_PENDING key value is kept to old value to avoid migration issues
	private static final String KEY_LEITFADEN_OPEN_PENDING = "hotline_call_pending";

	private static final String KEY_PENDING_REPORTS_HEADER_ANIMATION = "pending_reports_header_animation";
	private static final String KEY_CONFIG_FORCE_UPDATE = "config_do_force_update";
	private static final String KEY_CONFIG_HAS_INFOBOX = "has_ghettobox_v2";
	private static final String KEY_CONFIG_INFOBOX_COLLECTION = "ghettobox_collection";
	private static final String KEY_CONFIG_TEST_INFORMATION_URLS = "testinformation_urls";
	private static final String KEY_ONBOARDING_USER_NOT_IN_PILOT_GROUP = "user_is_not_in_pilot_group";
	private static final String KEY_LAST_CONFIG_LOAD_SUCCESS = "last_config_load_success";
	private static final String KEY_LAST_CONFIG_LOAD_SUCCESS_APP_VERSION = "last_config_load_success_app_version";
	private static final String KEY_LAST_CONFIG_LOAD_SUCCESS_SDK_INT = "last_config_load_success_sdk_int";
	private static final String KEY_T_DUMMY = "KEY_T_DUMMY";
	private static final String KEY_WHAT_TO_DO_POSITIVE_TEST_TEXTS = "whatToDoPositiveTestTexts";
	private static final String KEY_TEST_LOCATIONS = "test_locations";
	private static final String KEY_INTEROP_COUNTRIES = "interop_countries";
	private static final String KEY_APP_OPEN_AFTER_NOTIFICATION_PENDING = "appOpenAfterNotificationPending";
	private static final String KEY_ISOLATION_END_DIALOG_TIMESTAMP = "isolation_end_dialog_timestamp";
	private static final String KEY_APP_VERSION_CODE = "app_version_code";
	private static final String KEY_SCHEDULED_FAKE_WORKER_NAME = "scheduled_fake_worker_name";
	private static final String KEY_POSITIVE_REPORT_OLDEST_SHARED_KEY = "positive_report_oldest_shared_key";
	private static final String KEY_CURRENT_CHECK_IN = "KEY_CURRENT_CHECK_IN";
	private static final String KEY_CROWD_NOTIFIER_LAST_KEY_BUNDLE_TAG = "KEY_CROWD_NOTIFIER_LAST_KEY_BUNDLE_TAG";
	private static final String KEY_LAST_SUCCESSFUL_CHECKIN_DOWNLOAD = "KEY_LAST_SUCCESSFUL_CHECKIN_DOWNLOAD";
	private static final String KEY_ONLY_PARTIAL_ONBOARDING_DONE = "KEY_ONLY_PARTIAL_ONBOARDING_DONE";
	private static final String KEY_POSITIVE_REPORT_OLDEST_SHARED_KEY_OR_CHECKIN =
			"KEY_POSITIVE_REPORT_OLDEST_SHARED_KEY_OR_CHECKIN";


	private static SecureStorage instance;

	private SharedPreferences prefs;

	private Gson gson = new Gson();
	private Context context;

	private final MutableLiveData<Boolean> forceUpdateLiveData;
	private final MutableLiveData<Boolean> hasInfoboxLiveData;

	private SecureStorage(@NonNull Context context) {
		this.context = context;
		this.prefs = initializeSharedPreferences(context);

		forceUpdateLiveData = new MutableLiveData<>(getDoForceUpdate());
		hasInfoboxLiveData = new MutableLiveData<>(getHasInfobox());
	}

	public static synchronized SecureStorage getInstance(Context context) {
		if (instance == null) {
			instance = new SecureStorage(context);
		}
		return instance;
	}

	public LiveData<Boolean> getForceUpdateLiveData() {
		return forceUpdateLiveData;
	}

	public LiveData<Boolean> getInfoBoxLiveData() {
		return hasInfoboxLiveData;
	}

	public long getInfectedDate() {
		return prefs.getLong(KEY_INFECTED_DATE, 0);
	}

	public void setInfectedDate(long date) {
		prefs.edit().putLong(KEY_INFECTED_DATE, date).apply();
	}

	public void saveInformTimeAndCodeAndToken(String informCode, String dp3tInformToken, String checkinInformToken) {
		prefs.edit().putLong(KEY_INFORM_TIME_REQ, System.currentTimeMillis())
				.putString(KEY_INFORM_CODE_REQ, informCode)
				.putString(KEY_INFORM_TOKEN_REQ, dp3tInformToken)
				.putString(KEY_INFORM_TOKEN_CHECKIN_REQ, checkinInformToken)
				.apply();
	}

	public void clearInformTimeAndCodeAndToken() {
		prefs.edit().remove(KEY_INFORM_TIME_REQ)
				.remove(KEY_INFORM_CODE_REQ)
				.remove(KEY_INFORM_TOKEN_REQ)
				.remove(KEY_INFORM_TOKEN_CHECKIN_REQ)
				.apply();
	}

	public long getLastInformRequestTime() {
		return prefs.getLong(KEY_INFORM_TIME_REQ, 0);
	}

	public String getLastInformCode() {
		return prefs.getString(KEY_INFORM_CODE_REQ, null);
	}

	public String getLastDP3TInformToken() {
		return prefs.getString(KEY_INFORM_TOKEN_REQ, null);
	}

	public String getLastCheckinInformToken() {
		return prefs.getString(KEY_INFORM_TOKEN_CHECKIN_REQ, null);
	}

	public boolean getOnboardingCompleted() {
		return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
	}

	public void setOnboardingCompleted(boolean completed) {
		prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
	}

	public boolean getOnlyPartialOnboardingCompleted() {
		return prefs.getBoolean(KEY_ONLY_PARTIAL_ONBOARDING_DONE, false);
	}

	public void setOnlyPartialOnboardingCompleted(boolean completed) {
		prefs.edit().putBoolean(KEY_ONLY_PARTIAL_ONBOARDING_DONE, completed).apply();
	}

	public int getLastShownUpdateBoardingVersion() {
		return prefs.getInt(KEY_UPDATE_BOARDING_VERSION, 0);
	}

	public void setLastShownUpdateBoardingVersion(int version) {
		prefs.edit().putInt(KEY_UPDATE_BOARDING_VERSION, version).apply();
	}

	public int getLastShownContactId() {
		return prefs.getInt(KEY_LAST_SHOWN_CONTACT_ID, -1);
	}

	public void setLastSuccessfulCheckinDownload(long time) {
		prefs.edit().putLong(KEY_LAST_SUCCESSFUL_CHECKIN_DOWNLOAD, time).apply();
	}

	public long getLastSuccessfulCheckinDownload() {
		return prefs.getLong(KEY_LAST_SUCCESSFUL_CHECKIN_DOWNLOAD, System.currentTimeMillis());
	}

	public void setLastShownContactId(int contactId) {
		prefs.edit().putInt(KEY_LAST_SHOWN_CONTACT_ID, contactId).apply();
	}

	public boolean isOpenLeitfadenPending() {
		return prefs.getBoolean(KEY_LEITFADEN_OPEN_PENDING, false);
	}

	public void setLeitfadenOpenPending(boolean pending) {
		prefs.edit().putBoolean(KEY_LEITFADEN_OPEN_PENDING, pending).apply();
	}

	public void leitfadenOpened() {
		prefs.edit().putBoolean(KEY_LEITFADEN_OPEN_PENDING, false).apply();
	}

	public boolean isReportsHeaderAnimationPending() {
		return prefs.getBoolean(KEY_PENDING_REPORTS_HEADER_ANIMATION, false);
	}

	public void setReportsHeaderAnimationPending(boolean pending) {
		prefs.edit().putBoolean(KEY_PENDING_REPORTS_HEADER_ANIMATION, pending).apply();
	}

	public void setDoForceUpdate(boolean doForceUpdate) {
		prefs.edit().putBoolean(KEY_CONFIG_FORCE_UPDATE, doForceUpdate).apply();
		forceUpdateLiveData.postValue(doForceUpdate);
	}

	public boolean getDoForceUpdate() {
		return prefs.getBoolean(KEY_CONFIG_FORCE_UPDATE, false);
	}

	public void setHasInfobox(boolean hasInfobox) {
		prefs.edit().putBoolean(KEY_CONFIG_HAS_INFOBOX, hasInfobox).apply();
		hasInfoboxLiveData.postValue(hasInfobox);
	}

	public boolean getHasInfobox() {
		return prefs.getBoolean(KEY_CONFIG_HAS_INFOBOX, false);
	}

	public void setInfoBoxCollection(InfoBoxModelCollection infoBoxModelCollection) {
		prefs.edit().putString(KEY_CONFIG_INFOBOX_COLLECTION, gson.toJson(infoBoxModelCollection)).apply();
	}

	public InfoBoxModelCollection getInfoBoxCollection() {
		return gson.fromJson(prefs.getString(KEY_CONFIG_INFOBOX_COLLECTION, "null"), InfoBoxModelCollection.class);
	}

	public void setTestInformationUrls(Map<String, String> testInformationUrls) {
		prefs.edit().putString(KEY_CONFIG_TEST_INFORMATION_URLS, gson.toJson(testInformationUrls)).apply();
	}

	public String getTestInformationUrl(String languageKey) {
		String defaultUrl = "https://www.bag.admin.ch/bag/de/home/krankheiten/ausbrueche-epidemien-pandemien/" +
				"aktuelle-ausbrueche-epidemien/novel-cov/testen.html";

		Type testInformationsType = new TypeToken<Map<String, String>>() { }.getType();
		Map<String, String> testInfoMap =
				gson.fromJson(prefs.getString(KEY_CONFIG_TEST_INFORMATION_URLS, "null"), testInformationsType);
		if (testInfoMap == null || !testInfoMap.containsKey(languageKey)) {
			return defaultUrl;
		} else {
			return testInfoMap.get(languageKey);
		}
	}

	public boolean isUserNotInPilotGroup() {
		return prefs.getBoolean(KEY_ONBOARDING_USER_NOT_IN_PILOT_GROUP, false);
	}

	public void setUserNotInPilotGroup(boolean notInPilotGroup) {
		prefs.edit().putBoolean(KEY_ONBOARDING_USER_NOT_IN_PILOT_GROUP, notInPilotGroup).apply();
	}

	public long getLastConfigLoadSuccess() {
		return prefs.getLong(KEY_LAST_CONFIG_LOAD_SUCCESS, 0);
	}

	public void setLastConfigLoadSuccess(long time) {
		prefs.edit().putLong(KEY_LAST_CONFIG_LOAD_SUCCESS, time).apply();
	}

	public int getLastConfigLoadSuccessAppVersion() {
		return prefs.getInt(KEY_LAST_CONFIG_LOAD_SUCCESS_APP_VERSION, 0);
	}

	public void setLastConfigLoadSuccessAppVersion(int appVersion) {
		prefs.edit().putInt(KEY_LAST_CONFIG_LOAD_SUCCESS_APP_VERSION, appVersion).apply();
	}

	public int getLastConfigLoadSuccessSdkInt() {
		return prefs.getInt(KEY_LAST_CONFIG_LOAD_SUCCESS_SDK_INT, 0);
	}

	public void setLastConfigLoadSuccessSdkInt(int sdkInt) {
		prefs.edit().putInt(KEY_LAST_CONFIG_LOAD_SUCCESS_SDK_INT, sdkInt).apply();
	}

	public long getTDummy() { return prefs.getLong(KEY_T_DUMMY, -1); }

	public void setTDummy(long time) {
		prefs.edit().putLong(KEY_T_DUMMY, time).apply();
	}

	public void setWhatToDoPositiveTestTexts(WhatToDoPositiveTestTextsCollection whatToDoPositiveTestTexts) {
		prefs.edit().putString(KEY_WHAT_TO_DO_POSITIVE_TEST_TEXTS, gson.toJson(whatToDoPositiveTestTexts)).apply();
	}

	public WhatToDoPositiveTestTextsModel getWhatToDoPositiveTestTexts(String language) {
		HashMap<String, WhatToDoPositiveTestTextsModel> map =
				gson.fromJson(prefs.getString(KEY_WHAT_TO_DO_POSITIVE_TEST_TEXTS, "null"),
						WhatToDoPositiveTestTextsCollection.class);
		if (map == null) {
			return null;
		}
		return map.get(language);
	}

	public void setTestLocations(Map<String, List<TestLocationModel>> testLocations) {
		prefs.edit().putString(KEY_TEST_LOCATIONS, gson.toJson(testLocations)).apply();
	}

	public Map<String, List<TestLocationModel>> getTestLocations() {
		Type testLocationsType = new TypeToken<Map<String, List<TestLocationModel>>>() { }.getType();
		return gson.fromJson(prefs.getString(KEY_TEST_LOCATIONS, getDefaultTestLocations()), testLocationsType);
	}

	public void setInteropCountries(List<String> interopCountries) {
		prefs.edit().putString(KEY_INTEROP_COUNTRIES, gson.toJson(interopCountries)).apply();
	}

	public List<String> getInteropCountries() {
		Type interopCountriesType = new TypeToken<List<String>>() { }.getType();
		List<String> countries = gson.fromJson(prefs.getString(KEY_INTEROP_COUNTRIES, "[]"), interopCountriesType);
		if (countries != null) {
			return countries;
		} else {
			return new ArrayList<>();
		}
	}

	private String getDefaultTestLocations() {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(context.getAssets().open(DEFAULT_TEST_LOCATIONS_JSON_PATH), StandardCharsets.UTF_8));
		) {
			StringBuilder sb = new StringBuilder();
			String str;
			while ((str = br.readLine()) != null) { sb.append(str); }
			return sb.toString();
		} catch (IOException e) {
			return null;
		}
	}

	public void setAppOpenAfterNotificationPending(boolean pending) {
		prefs.edit().putBoolean(KEY_APP_OPEN_AFTER_NOTIFICATION_PENDING, pending).apply();
	}

	public boolean getAppOpenAfterNotificationPending() {
		return prefs.getBoolean(KEY_APP_OPEN_AFTER_NOTIFICATION_PENDING, false);
	}

	public long getIsolationEndDialogTimestamp() {
		return prefs.getLong(KEY_ISOLATION_END_DIALOG_TIMESTAMP, -1L);
	}

	public void setIsolationEndDialogTimestamp(long timestamp) {
		prefs.edit().putLong(KEY_ISOLATION_END_DIALOG_TIMESTAMP, timestamp).apply();
	}

	public void setLastKnownAppVersionCode(int versionCode) {
		prefs.edit().putInt(KEY_APP_VERSION_CODE, versionCode).apply();
	}

	public int getLastKnownAppVersionCode() {
		return prefs.getInt(KEY_APP_VERSION_CODE, -1);
	}

	public void setScheduledFakeWorkerName(String workerName) {
		prefs.edit().putString(KEY_SCHEDULED_FAKE_WORKER_NAME, workerName).apply();
	}

	public String getScheduledFakeWorkerName() {
		return prefs.getString(KEY_SCHEDULED_FAKE_WORKER_NAME, null);
	}

	public long getPositiveReportOldestSharedKey() {
		return prefs.getLong(KEY_POSITIVE_REPORT_OLDEST_SHARED_KEY, -1L);
	}

	public void setPositiveReportOldestSharedKey(long setPositiveReportOldestSharedKey) {
		prefs.edit().putLong(KEY_POSITIVE_REPORT_OLDEST_SHARED_KEY, setPositiveReportOldestSharedKey).apply();
	}

	public long getPositiveReportOldestSharedKeyOrCheckin() {
		return prefs.getLong(KEY_POSITIVE_REPORT_OLDEST_SHARED_KEY_OR_CHECKIN, -1L);
	}

	public void setPositiveReportOldestSharedKeyOrCheckin(long oldestSharedKeyOrCheckin) {
		prefs.edit().putLong(KEY_POSITIVE_REPORT_OLDEST_SHARED_KEY_OR_CHECKIN, oldestSharedKeyOrCheckin).apply();
	}

	private synchronized SharedPreferences initializeSharedPreferences(@NonNull Context context) {
		try {
			return createEncryptedSharedPreferences(context);
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setCheckInState(CheckInState checkInState) {
		prefs.edit().putString(KEY_CURRENT_CHECK_IN, gson.toJson(checkInState)).apply();
	}

	public CheckInState getCheckInState() {
		return gson.fromJson(prefs.getString(KEY_CURRENT_CHECK_IN, null), CheckInState.class);
	}

	public void setCrowdNotifierLastKeyBundleTag(long lastSync) {
		prefs.edit().putLong(KEY_CROWD_NOTIFIER_LAST_KEY_BUNDLE_TAG, lastSync).apply();
	}

	public long getCrowdNotifierLastKeyBundleTag() {
		return prefs.getLong(KEY_CROWD_NOTIFIER_LAST_KEY_BUNDLE_TAG, 0);
	}


	/**
	 * Create or obtain an encrypted SharedPreferences instance. Note that this method is synchronized because the AndroidX
	 * Security
	 * library is not thread-safe.
	 * @see <a href="https://developer.android.com/topic/security/data">https://developer.android.com/topic/security/data</a>
	 */
	private synchronized SharedPreferences createEncryptedSharedPreferences(@NonNull Context context)
			throws GeneralSecurityException, IOException {
		String masterKeys = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
		return EncryptedSharedPreferences
				.create(PREFERENCES, masterKeys, context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
						EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
	}

}