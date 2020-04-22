package org.dpppt.android.app.main;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

		if (savedInstanceState == null) {
			getChildFragmentManager()
					.beginTransaction()
					.add(R.id.main_fragment_container, HomeFragment.newInstance())
					.commit();
		}
	}

}
