package org.dpppt.android.app.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.MainActivity;
import org.dpppt.android.app.R;
import org.dpppt.android.app.reports.ReportsFragment;
import org.dpppt.android.app.storage.SecureStorage;

public class MainFragment extends Fragment {

	public static MainFragment newInstance() {
		return new MainFragment();
	}

	public MainFragment() {
		super(R.layout.fragment_main);
	}

	private static final String STATE_CONSUMED_INTENT = "STATE_CONSUMED_INTENT";
	private boolean consumedIntent;

	private SecureStorage secureStorage;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		secureStorage = SecureStorage.getInstance(getContext());

		if (savedInstanceState == null) {
			getChildFragmentManager()
					.beginTransaction()
					.add(R.id.main_fragment_container, HomeFragment.newInstance())
					.commit();
		} else {
			consumedIntent = savedInstanceState.getBoolean(STATE_CONSUMED_INTENT);
		}
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
		MainActivity mainActivity = (MainActivity) getActivity();
		Intent intent = mainActivity.getIntent();
		String intentAction = intent.getAction();
		boolean launchedFromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
		if (intentAction != null && !launchedFromHistory && !consumedIntent) {
			consumedIntent = true;
			if (intentAction.equals(MainActivity.ACTION_GOTO_REPORTS)) {
				gotoReportsFragment();
			}
			intent.setAction(null);
			mainActivity.setIntent(intent);
		}
	}

	private void gotoReportsFragment() {
		getChildFragmentManager()
				.beginTransaction()
				.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
				.addToBackStack(ReportsFragment.class.getCanonicalName())
				.commit();
	}

}
