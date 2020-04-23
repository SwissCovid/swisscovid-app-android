/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.reports;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.dpppt.android.app.R;
import org.dpppt.android.app.onboarding.OnboardingBatteryPermissionFragment;
import org.dpppt.android.app.onboarding.OnboardingContentFragment;
import org.dpppt.android.app.onboarding.OnboardingFinishedFragment;
import org.dpppt.android.app.onboarding.OnboardingLocationPermissionFragment;

public class ReportsSlidePageAdapter extends FragmentStateAdapter {

	public ReportsSlidePageAdapter(Fragment fragment) {
		super(fragment);
	}

	@NonNull
	@Override
	public Fragment createFragment(int position) {
		switch (position) {
			case 0:
				return new Fragment();
			case 1:
				return new Fragment();
		}
		throw new IllegalArgumentException("There is no fragment for view pager position " + position);
	}

	@Override
	public int getItemCount() {
		return 2;
	}

}
