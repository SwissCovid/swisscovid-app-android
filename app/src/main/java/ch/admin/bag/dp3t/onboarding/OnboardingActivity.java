/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.onboarding;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.dpppt.android.sdk.DP3T;

import ch.admin.bag.dp3t.R;

public class OnboardingActivity extends FragmentActivity {

	private ViewPager2 viewPager;
	private FragmentStateAdapter pagerAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_onboarding);

		viewPager = findViewById(R.id.pager);
		viewPager.setUserInputEnabled(false);
		pagerAdapter = new OnboardingSlidePageAdapter(this);
		viewPager.setAdapter(pagerAdapter);
	}

	public void continueToNextPage() {
		int currentItem = viewPager.getCurrentItem();
		if (currentItem < pagerAdapter.getItemCount() - 1) {
			viewPager.setCurrentItem(currentItem + 1, true);
		} else {
			DP3T.start(this);
			setResult(RESULT_OK);
			finish();
		}
	}
}
