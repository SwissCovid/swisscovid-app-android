/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.app.main.HomeFragment;
import org.dpppt.android.app.networking.ConfigWorker;
import org.dpppt.android.app.onboarding.OnboardingActivity;
import org.dpppt.android.app.reports.ReportsFragment;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.app.util.InfoDialog;
import org.dpppt.android.app.viewmodel.TracingViewModel;

public class MainActivity extends FragmentActivity {

	public static final String ACTION_GOTO_REPORTS = "ACTION_GOTO_REPORTS";

	private static final int REQ_ONBOARDING = 123;

	private static final String STATE_CONSUMED_INTENT = "STATE_CONSUMED_INTENT";
	private boolean consumedIntent;

	private SecureStorage secureStorage;

	private TracingViewModel tracingViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		secureStorage = SecureStorage.getInstance(this);

		secureStorage.getForceUpdateLiveData().observe(this, forceUpdate -> {
			forceUpdate = forceUpdate && secureStorage.getDoForceUpdate();
			InfoDialog forceUpdateDialog =
					(InfoDialog) getSupportFragmentManager().findFragmentByTag(InfoDialog.class.getCanonicalName());
			if (forceUpdate && forceUpdateDialog == null) {
				forceUpdateDialog = InfoDialog.newInstanceWithButtonLabel(R.string.force_update_text, R.string.force_update_title);
				forceUpdateDialog.setCancelable(false);
				forceUpdateDialog.setButtonOnClickListener(v -> {
					String packageName = getPackageName();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=" + packageName));
					if (intent.resolveActivity(getPackageManager()) != null) {
						startActivity(intent);
					}
				});
				forceUpdateDialog.show(getSupportFragmentManager(), InfoDialog.class.getCanonicalName());
			} else if (!forceUpdate && forceUpdateDialog != null) {
				forceUpdateDialog.dismiss();
			}
		});
		ConfigWorker.startConfigWorker(this);

		if (savedInstanceState == null) {
			boolean onboardingCompleted = secureStorage.getOnboardingCompleted();
			if (onboardingCompleted) {
				showHomeFragment();
			} else {
				startActivityForResult(new Intent(this, OnboardingActivity.class), REQ_ONBOARDING);
			}
		} else {
			consumedIntent = savedInstanceState.getBoolean(STATE_CONSUMED_INTENT);
		}

		tracingViewModel = new ViewModelProvider(this).get(TracingViewModel.class);
		tracingViewModel.sync();
	}

	@Override
	public void onResume() {
		super.onResume();

		checkIntentForActions();

		if (!consumedIntent) {
			boolean isHotlineCallPending = secureStorage.isHotlineCallPending();
			if (isHotlineCallPending) {
				gotoReportsFragment();
			}
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_CONSUMED_INTENT, consumedIntent);
	}

	private void checkIntentForActions() {
		Intent intent = getIntent();
		String intentAction = intent.getAction();
		boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
		if (intentAction != null && !launchedFromHistory && !consumedIntent) {
			consumedIntent = true;
			if (intentAction.equals(MainActivity.ACTION_GOTO_REPORTS)) {
				gotoReportsFragment();
			}
			intent.setAction(null);
			setIntent(intent);
		}
	}

	private void showHomeFragment() {
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.main_fragment_container, HomeFragment.newInstance())
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
