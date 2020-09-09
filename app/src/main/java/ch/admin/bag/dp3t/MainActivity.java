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

import ch.admin.bag.dp3t.networking.ConfigWorker;
import ch.admin.bag.dp3t.onboarding.OnboardingActivity;
import ch.admin.bag.dp3t.reports.ReportsFragment;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class MainActivity extends FragmentActivity {

	public static final String ACTION_EXPOSED_GOTO_REPORTS = "ACTION_EXPOSED_GOTO_REPORTS";
	public static final String ACTION_INFORMED_GOTO_REPORTS = "ACTION_INFORMED_GOTO_REPORTS";

	private static final int REQ_ONBOARDING = 123;

	private static final String STATE_CONSUMED_EXPOSED_INTENT = "STATE_CONSUMED_EXPOSED_INTENT";
	private boolean consumedExposedIntent;

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
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("market://details?id=" + packageName));
							if (intent.resolveActivity(getPackageManager()) != null) {
								startActivity(intent);
							}
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
				showMainFragment();
			} else {
				startActivityForResult(new Intent(this, OnboardingActivity.class), REQ_ONBOARDING);
			}
		} else {
			consumedExposedIntent = savedInstanceState.getBoolean(STATE_CONSUMED_EXPOSED_INTENT);
		}

		tracingViewModel = new ViewModelProvider(this).get(TracingViewModel.class);
		tracingViewModel.sync();

		checkRedirectionIntents();
	}

	public void checkRedirectionIntents() {
		checkIntentForActions();

		if (!consumedExposedIntent) {
			boolean isHotlineCallPending = secureStorage.isHotlineCallPending();
			boolean isExposed = tracingViewModel.getTracingStatusInterface().wasContactReportedAsExposed();
			if (isHotlineCallPending && isExposed) {
				gotoReportsFragment();
			}
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_CONSUMED_EXPOSED_INTENT, consumedExposedIntent);
	}

	private void checkIntentForActions() {
		Intent intent = getIntent();
		String intentAction = intent.getAction();
		boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
		if (ACTION_INFORMED_GOTO_REPORTS.equals(intentAction) && !launchedFromHistory) {
			secureStorage.setHotlineCallPending(false);
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
		}
	}

	private void showMainFragment() {
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.root_fragment_container, MainFragment.newInstance())
				.commit();
	}

	private void gotoReportsFragment() {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.root_fragment_container, ReportsFragment.newInstance())
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
				showMainFragment();
			} else {
				finish();
			}
		}
	}

}
