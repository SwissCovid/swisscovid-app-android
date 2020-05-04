/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import ch.admin.bag.dp3t.main.HomeFragment;
import ch.admin.bag.dp3t.networking.ConfigWorker;
import ch.admin.bag.dp3t.onboarding.OnboardingActivity;
import ch.admin.bag.dp3t.reports.ReportsFragment;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.InfoDialog;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class MainActivity extends FragmentActivity {

	public static final String ACTION_GOTO_REPORTS = "ACTION_GOTO_REPORTS";
	public static final String ACTION_STOP_TRACING = "ACTION_STOP_TRACING";

	private static final int REQ_ONBOARDING = 123;

	private static final String STATE_CONSUMED_EXPOSED_INTENT = "STATE_CONSUMED_EXPOSED_INTENT";
	private boolean consumedExposedIntent;

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
				forceUpdateDialog = InfoDialog.newInstance(R.string.force_update_text);
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
			consumedExposedIntent = savedInstanceState.getBoolean(STATE_CONSUMED_EXPOSED_INTENT);
		}

		tracingViewModel = new ViewModelProvider(this).get(TracingViewModel.class);
		tracingViewModel.sync();
	}

	@Override
	public void onResume() {
		super.onResume();

		checkIntentForActions();

		if (!consumedExposedIntent) {
			boolean isHotlineCallPending = secureStorage.isHotlineCallPending();
			if (isHotlineCallPending) {
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
		if (ACTION_STOP_TRACING.equals(intentAction) && !launchedFromHistory) {
			tracingViewModel.setTracingEnabled(false);
			intent.setAction(null);
			setIntent(intent);
		}
		else if (ACTION_GOTO_REPORTS.equals(intentAction) && !launchedFromHistory && !consumedExposedIntent) {
			consumedExposedIntent = true;
			gotoReportsFragment();
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
