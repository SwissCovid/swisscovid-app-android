/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.onboarding;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.ScrollView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.dpppt.android.sdk.DP3T;

import org.dpppt.android.app.R;

public class OnboardingActivity extends FragmentActivity {

	private ViewPager2 viewPager;
	private FragmentStateAdapter pagerAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_onboarding);

		viewPager = findViewById(R.id.pager);
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

	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() == 0) {
			setResult(RESULT_CANCELED);
			finish();
		} else {
			viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
		}
	}

}
