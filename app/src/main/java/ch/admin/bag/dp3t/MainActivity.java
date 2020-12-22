/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.sdk.DP3T;

import ch.admin.bag.dp3t.home.model.TracingStatusInterface;
import ch.admin.bag.dp3t.inform.InformActivity;
import ch.admin.bag.dp3t.networking.ConfigWorker;
import ch.admin.bag.dp3t.onboarding.OnboardingActivity;
import ch.admin.bag.dp3t.reports.ReportsFragment;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.UrlUtil;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;
import ch.admin.bag.dp3t.whattodo.WtdPositiveTestFragment;

import static ch.admin.bag.dp3t.inform.InformActivity.EXTRA_COVIDCODE;
import static ch.admin.bag.dp3t.util.NotificationUtil.ACTION_ACTIVATE_TRACING;

public class MainActivity extends FragmentActivity {

	public static final String ACTION_EXPOSED_GOTO_REPORTS = "ACTION_EXPOSED_GOTO_REPORTS";
	public static final String ACTION_INFORMED_GOTO_REPORTS = "ACTION_INFORMED_GOTO_REPORTS";

	private static final int REQ_ONBOARDING = 123;

	private static final String STATE_CONSUMED_EXPOSED_INTENT = "STATE_CONSUMED_EXPOSED_INTENT";
	private static final String STATE_CONSUMED_COVIDCODE_INTENT = "STATE_CONSUMED_COVIDCODE_INTENT";
	private boolean consumedExposedIntent;
	private boolean consumedCovidcodeIntent;

	private SecureStorage secureStorage;
	private TracingViewModel tracingViewModel;

	private AlertDialog forceUpdateDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		secureStorage = SecureStorage.getInstance(this);

		secureStorage.getForceUpdateLiveData().observe(this, forceUpdate -> {
			forceUpdate = forceUpdate && secureStorage.getDoForceUpdate();
			if (forceUpdate && forceUpdateDialog == null) {
				forceUpdateDialog = new AlertDialog.Builder(this, R.style.NextStep_AlertDialogStyle)
						.setTitle(R.string.force_update_title)
						.setMessage(R.string.force_update_text)
						.setPositiveButton(R.string.playservices_update, null)
						.setCancelable(false)
						.create();
				forceUpdateDialog.setOnShowListener(dialog ->
						forceUpdateDialog.getButton(DialogInterface.BUTTON_POSITIVE)
								.setOnClickListener(v -> {
									String packageName = getPackageName();
									UrlUtil.openUrl(MainActivity.this, "market://details?id=" + packageName);
								}));
				forceUpdateDialog.show();
			} else if (!forceUpdate && forceUpdateDialog != null) {
				forceUpdateDialog.dismiss();
				forceUpdateDialog = null;
			}
		});

		ConfigWorker.scheduleConfigWorkerIfOutdated(this);

		if (savedInstanceState == null) {
			boolean onboardingCompleted = secureStorage.getOnboardingCompleted();
			if (onboardingCompleted) {
				showHomeFragment();
			} else {
				startActivityForResult(new Intent(this, OnboardingActivity.class), REQ_ONBOARDING);
			}
		} else {
			consumedExposedIntent = savedInstanceState.getBoolean(STATE_CONSUMED_EXPOSED_INTENT);
			consumedCovidcodeIntent = savedInstanceState.getBoolean(STATE_CONSUMED_COVIDCODE_INTENT);
		}

		tracingViewModel = new ViewModelProvider(this).get(TracingViewModel.class);
		tracingViewModel.sync();

		checkRedirectionIntents();
	}

	public void checkRedirectionIntents() {
		checkIntentForActions();

		if (!consumedExposedIntent) {
			boolean isOpenLeitfadenPending = secureStorage.isOpenLeitfadenPending();
			boolean isExposed = tracingViewModel.getTracingStatusInterface().wasContactReportedAsExposed();
			if (isOpenLeitfadenPending && isExposed) {
				gotoReportsFragment();
			}
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_CONSUMED_EXPOSED_INTENT, consumedExposedIntent);
		outState.putBoolean(STATE_CONSUMED_COVIDCODE_INTENT, consumedCovidcodeIntent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		consumedCovidcodeIntent = false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		secureStorage.setAppOpenAfterNotificationPending(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (secureStorage.getOnboardingCompleted()) checkValidCovidcodeIntent();
	}

	private void checkIntentForActions() {
		Intent intent = getIntent();
		String intentAction = intent.getAction();
		boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
		if (ACTION_INFORMED_GOTO_REPORTS.equals(intentAction) && !launchedFromHistory) {
			secureStorage.setLeitfadenOpenPending(false);
			secureStorage.setReportsHeaderAnimationPending(false);
			gotoReportsFragment();
			intent.setAction(null);
			setIntent(intent);
		} else if (ACTION_EXPOSED_GOTO_REPORTS.equals(intentAction) && !launchedFromHistory && !consumedExposedIntent) {
			consumedExposedIntent = true;
			intent.setAction(null);
			setIntent(intent);
			if (tracingViewModel.getTracingStatusInterface().wasContactReportedAsExposed()) {
				gotoReportsFragment();
			}
		} else if (ACTION_ACTIVATE_TRACING.equals(intentAction)) {
			tracingViewModel.enableTracing(this, () -> {}, e -> {}, () -> {});
		}
	}

	private void checkValidCovidcodeIntent() {
		boolean launchedFromHistory = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
		TracingStatusInterface tracingStatus = tracingViewModel.getAppStatusLiveData().getValue();
		if (getIntent().getData() == null || launchedFromHistory || tracingStatus == null || tracingStatus.isReportedAsInfected() ||
				consumedCovidcodeIntent) {
			return;
		}
		Uri uri = Uri.parse(getIntent().getData().toString());
		if (!uri.getHost().equals("cc.admin.ch")) return;
		if (!uri.getPath().equals("") && !uri.getPath().equals("/")) return;
		String covidCode = uri.getFragment();
		if (covidCode == null || covidCode.length() != 12) return;
		startInformFlow(covidCode);
		consumedCovidcodeIntent = true;
	}

	private void startInformFlow(String covidCode) {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_fragment_container, WtdPositiveTestFragment.newInstance())
				.addToBackStack(WtdPositiveTestFragment.class.getCanonicalName())
				.commit();
		Intent intent = new Intent(this, InformActivity.class);
		intent.putExtra(EXTRA_COVIDCODE, covidCode);
		startActivity(intent);
	}

	private void showHomeFragment() {
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.main_fragment_container, TabbarHostFragment.newInstance())
				.commit();
	}

	private void gotoReportsFragment() {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
				.addToBackStack(ReportsFragment.class.getCanonicalName())
				.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DP3T.onActivityResult(this, requestCode, resultCode, data);
		if (requestCode == REQ_ONBOARDING) {
			if (resultCode == RESULT_OK) {
				secureStorage.setOnboardingCompleted(true);
				showHomeFragment();
			} else {
				finish();
			}
		}
	}

}
