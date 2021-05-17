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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.crowdnotifier.android.sdk.CrowdNotifier;
import org.crowdnotifier.android.sdk.model.VenueInfo;
import org.crowdnotifier.android.sdk.utils.QrUtils;
import org.dpppt.android.sdk.DP3T;

import ch.admin.bag.dp3t.checkin.CheckinOverviewFragment;
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel;
import ch.admin.bag.dp3t.checkin.checkinflow.CheckInFragment;
import ch.admin.bag.dp3t.checkin.checkinflow.CheckOutFragment;
import ch.admin.bag.dp3t.checkin.models.CheckInState;
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState;
import ch.admin.bag.dp3t.checkin.networking.CrowdNotifierKeyLoadWorker;
import ch.admin.bag.dp3t.checkin.utils.ErrorDialog;
import ch.admin.bag.dp3t.home.model.TracingStatusInterface;
import ch.admin.bag.dp3t.inform.InformActivity;
import ch.admin.bag.dp3t.networking.ConfigWorker;
import ch.admin.bag.dp3t.onboarding.OnboardingActivity;
import ch.admin.bag.dp3t.reports.ReportsFragment;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.updateboarding.UpdateBoardingActivity;
import ch.admin.bag.dp3t.util.UrlUtil;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;
import ch.admin.bag.dp3t.whattodo.WtdPositiveTestFragment;

import static ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper.ACTION_DID_AUTO_CHECKOUT;
import static ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper.autoCheckoutIfNecessary;
import static ch.admin.bag.dp3t.checkin.utils.NotificationHelper.ACTION_CHECK_OUT_NOW;
import static ch.admin.bag.dp3t.checkin.utils.NotificationHelper.ACTION_CROWDNOTIFIER_REMINDER_NOTIFICATION;
import static ch.admin.bag.dp3t.checkin.utils.NotificationHelper.ACTION_CROWDNOTIFIER_EXPOSURE_NOTIFICATION;
import static ch.admin.bag.dp3t.checkin.utils.NotificationHelper.ACTION_ONGOING_NOTIFICATION;
import static ch.admin.bag.dp3t.inform.InformActivity.EXTRA_COVIDCODE;
import static ch.admin.bag.dp3t.updateboarding.UpdateBoardingActivity.UPDATE_BOARDING_VERSION;
import static ch.admin.bag.dp3t.util.NotificationUtil.ACTION_ACTIVATE_TRACING;

public class MainActivity extends FragmentActivity {

	public static final String ACTION_EXPOSED_GOTO_REPORTS = "ACTION_EXPOSED_GOTO_REPORTS";
	public static final String ACTION_INFORMED_GOTO_REPORTS = "ACTION_INFORMED_GOTO_REPORTS";

	private static final String KEY_IS_INTENT_CONSUMED = "KEY_IS_INTENT_CONSUMED";
	private boolean isIntentConsumed = false;

	private SecureStorage secureStorage;
	private TracingViewModel tracingViewModel;
	private CrowdNotifierViewModel crowdNotifierViewModel;

	private AlertDialog forceUpdateDialog;


	private final ActivityResultLauncher<Intent> onAndUpdateBoardingLauncher =
			registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
				if (activityResult.getResultCode() == RESULT_OK) {
					secureStorage.setLastShownUpdateBoardingVersion(UPDATE_BOARDING_VERSION);
					secureStorage.setOnboardingCompleted(true);
					showHomeFragment();
				} else {
					finish();
				}
			});

	private BroadcastReceiver autoCheckoutBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			showHomeFragment();
		}
	};


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
		CrowdNotifierKeyLoadWorker.startKeyLoadWorker(this);
		CrowdNotifierKeyLoadWorker.cleanUpOldData(this);

		if (savedInstanceState == null) {
			boolean onboardingCompleted = secureStorage.getOnboardingCompleted();
			int lastShownUpdateBoardingVersion = secureStorage.getLastShownUpdateBoardingVersion();
			if (!onboardingCompleted) {
				onAndUpdateBoardingLauncher.launch(new Intent(this, OnboardingActivity.class));
			} else if (lastShownUpdateBoardingVersion < UPDATE_BOARDING_VERSION) {
				onAndUpdateBoardingLauncher.launch(new Intent(this, UpdateBoardingActivity.class));
			} else {
				showHomeFragment();
			}
		} else {
			isIntentConsumed = savedInstanceState.getBoolean(KEY_IS_INTENT_CONSUMED);
		}

		tracingViewModel = new ViewModelProvider(this).get(TracingViewModel.class);
		crowdNotifierViewModel = new ViewModelProvider(this).get(CrowdNotifierViewModel.class);
		tracingViewModel.sync();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_INTENT_CONSUMED, isIntentConsumed);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		isIntentConsumed = false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		secureStorage.setAppOpenAfterNotificationPending(false);
		crowdNotifierViewModel.refreshTraceKeys();
		autoCheckoutIfNecessary(this, crowdNotifierViewModel.getCheckInState());
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (secureStorage.getOnboardingCompleted()) checkIntentForActions();
		LocalBroadcastManager.getInstance(this)
				.registerReceiver(autoCheckoutBroadcastReceiver, new IntentFilter(ACTION_DID_AUTO_CHECKOUT));
	}

	private void checkIntentForActions() {
		Intent intent = getIntent();
		boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
		if (!launchedFromHistory && !isIntentConsumed) {
			isIntentConsumed = true;
			handleCustomIntents();
		}
	}

	private void handleCustomIntents() {
		String intentAction = getIntent().getAction();
		if (ACTION_INFORMED_GOTO_REPORTS.equals(intentAction)) {
			secureStorage.setLeitfadenOpenPending(false);
			secureStorage.setReportsHeaderAnimationPending(false);
			showReportsFragment();
		} else if (ACTION_EXPOSED_GOTO_REPORTS.equals(intentAction)) {
			if (tracingViewModel.getTracingStatusInterface().wasContactReportedAsExposed()) {
				showReportsFragment();
			}
		} else if (ACTION_ACTIVATE_TRACING.equals(intentAction)) {
			tracingViewModel.enableTracing(this, () -> {}, e -> {}, () -> {});
		} else if ((ACTION_CROWDNOTIFIER_REMINDER_NOTIFICATION.equals(intentAction) ||
				ACTION_ONGOING_NOTIFICATION.equals(intentAction)) && crowdNotifierViewModel.isCheckedIn().getValue()) {
			showCheckinOverviewFragment();
		} else if (ACTION_CHECK_OUT_NOW.equals(intentAction) && crowdNotifierViewModel.isCheckedIn().getValue()) {
			showCheckOutFragment();
		} else if (ACTION_CROWDNOTIFIER_EXPOSURE_NOTIFICATION.equals(intentAction)) {
			showReportsFragment();
		} else if (getIntent().getData() != null) {
			checkValidCovidcodeIntent();
			checkValidCheckInIntent();
		}
	}

	private void checkValidCheckInIntent() {
		String qrCodeData = getIntent().getDataString();
		try {
			VenueInfo venueInfo = CrowdNotifier.getVenueInfo(qrCodeData, BuildConfig.ENTRY_QR_CODE_PREFIX);
			if (crowdNotifierViewModel.isCheckedIn().getValue()) {
				new ErrorDialog(this, CrowdNotifierErrorState.ALREADY_CHECKED_IN).show();
			} else {
				crowdNotifierViewModel.setCheckInState(new CheckInState(false, venueInfo, System.currentTimeMillis(),
						System.currentTimeMillis(), 0));
				showCheckInScreen();
			}
		} catch (QrUtils.QRException e) {
			handleInvalidQRCodeExceptions(qrCodeData, e);
		}
	}

	private void handleInvalidQRCodeExceptions(String qrCodeData, QrUtils.QRException e) {
		if (e instanceof QrUtils.InvalidQRCodeVersionException) {
			new ErrorDialog(this, CrowdNotifierErrorState.UPDATE_REQUIRED).show();
		} else if (e instanceof QrUtils.NotYetValidException) {
			new ErrorDialog(this, CrowdNotifierErrorState.QR_CODE_NOT_YET_VALID).show();
		} else if (e instanceof QrUtils.NotValidAnymoreException) {
			new ErrorDialog(this, CrowdNotifierErrorState.QR_CODE_NOT_VALID_ANYMORE).show();
		} else {
			if (qrCodeData.startsWith(BuildConfig.TRACE_QR_CODE_PREFIX)) {
				Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrCodeData));
				startActivity(openBrowserIntent);
			} else {
				new ErrorDialog(this, CrowdNotifierErrorState.NO_VALID_QR_CODE).show();
			}
		}
	}


	private void checkValidCovidcodeIntent() {
		TracingStatusInterface tracingStatus = tracingViewModel.getAppStatusLiveData().getValue();
		if (tracingStatus == null || tracingStatus.isReportedAsInfected()) {
			return;
		}
		Uri uri = Uri.parse(getIntent().getData().toString());
		if (!uri.getHost().equals("cc.admin.ch")) return;
		if (!uri.getPath().equals("") && !uri.getPath().equals("/")) return;
		String covidCode = uri.getFragment();
		if (covidCode == null || covidCode.length() != 12) return;
		startInformFlow(covidCode);
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

	private void showReportsFragment() {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
				.addToBackStack(ReportsFragment.class.getCanonicalName())
				.commit();
	}

	private void showCheckinOverviewFragment() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_fragment_container, CheckinOverviewFragment.newInstance())
				.addToBackStack(CheckinOverviewFragment.class.getCanonicalName())
				.commit();
	}

	private void showCheckOutFragment() {
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.modal_slide_enter, R.anim.modal_slide_exit, R.anim.modal_pop_enter,
						R.anim.modal_pop_exit)
				.replace(R.id.main_fragment_container, CheckOutFragment.newInstance())
				.addToBackStack(CheckOutFragment.class.getCanonicalName())
				.commit();
	}

	private void showCheckInScreen() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_fragment_container, CheckInFragment.newInstance())
				.addToBackStack(CheckInFragment.class.getCanonicalName())
				.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DP3T.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(autoCheckoutBroadcastReceiver);
		super.onPause();
	}

}
