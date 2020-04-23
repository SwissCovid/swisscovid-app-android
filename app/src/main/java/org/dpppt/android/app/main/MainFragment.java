package org.dpppt.android.app.main;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.MainApplication;
import org.dpppt.android.app.R;

public class MainFragment extends Fragment {

	public static MainFragment newInstance() {
		return new MainFragment();
	}

	public MainFragment() {
		super(R.layout.fragment_main);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean launchReportsDirectly = false;
		Integer contactToShow = MainApplication.getAndClearContactToShowId(getContext());
		// TODO: clear only when really called because of this contact in reports-fragment! Otherwise keep this Id in preferences.
		if (contactToShow != null) {
			MainApplication.saveLaunchByContactId(getContext(), contactToShow);
			launchReportsDirectly = true;
		}

		if (savedInstanceState == null) {
			getChildFragmentManager()
					.beginTransaction()
					.add(R.id.main_fragment_container,
							HomeFragment.newInstance(launchReportsDirectly))
					.commit();
		}
	}

}
