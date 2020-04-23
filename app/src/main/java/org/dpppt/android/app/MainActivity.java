/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.dpppt.android.app.info.TheAppFragment;
import org.dpppt.android.app.main.MainFragment;
import org.dpppt.android.app.onboarding.OnboardingActivity;

public class MainActivity extends FragmentActivity {

	public static final String EXTRA_NOTIFICATION_CONTACT_ID = "EXTRA_NOTIFICATION_CONTACT_ID";

	private static final String PREFS_COVID = "PREFS_COVID";
	private static final String PREF_KEY_ONBOARDING_COMPLETED = "PREF_KEY_ONBOARDING_COMPLETED";

	private static final int REQ_ONBOARDING = 123;

	private static final int[] MENU_ACTION_IDS = new int[]{R.id.action_home, R.id.action_theapp};

	private BottomNavigationView bottomNavigationView;
	private ViewPager2 viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupNavigationViews();

		if (savedInstanceState == null) {
			SharedPreferences preferences = getSharedPreferences(PREFS_COVID, MODE_PRIVATE);
			boolean onboardingCompleted = preferences.getBoolean(PREF_KEY_ONBOARDING_COMPLETED, false);

			if (onboardingCompleted) {

				bottomNavigationView.setSelectedItemId(R.id.action_home);
			} else {
				startActivityForResult(new Intent(this, OnboardingActivity.class), REQ_ONBOARDING);
			}
		}
	}

	private void setupNavigationViews() {
		viewPager = findViewById(R.id.main_fragment_view_pager);
		bottomNavigationView = findViewById(R.id.main_bottom_navigation_view);

		FragmentStateAdapter fragmentStateAdapter = new MainNavigationStateAdapter(this);
		viewPager.setAdapter(fragmentStateAdapter);
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(int position) {
				bottomNavigationView.getMenu().findItem(MENU_ACTION_IDS[position]).setChecked(true);
			}
		});

		bottomNavigationView.inflateMenu(R.menu.navigation_menu);
		bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
			int index = -1;
			for (int i = 0; i < MENU_ACTION_IDS.length; i++) {
				if (MENU_ACTION_IDS[i] == item.getItemId()) {
					index = i;
					break;
				}
			}
			if (index >= 0) viewPager.setCurrentItem(index);
			return true;
		});
	}

	public ViewPager2 getMainViewPager() {
		return viewPager;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_ONBOARDING) {
			if (resultCode == RESULT_OK) {
				SharedPreferences preferences = getSharedPreferences(PREFS_COVID, MODE_PRIVATE);
				preferences.edit().putBoolean(PREF_KEY_ONBOARDING_COMPLETED, true).apply();
				bottomNavigationView.setSelectedItemId(R.id.action_home);
			} else {
				finish();
			}
		}
	}

	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();
		for (Fragment frag : fm.getFragments()) {
			if (frag.isVisible()) {
				FragmentManager childFm = frag.getChildFragmentManager();
				if (childFm.getBackStackEntryCount() > 0) {
					childFm.popBackStack();
					return;
				}
			}
		}
		super.onBackPressed();
	}

	private class MainNavigationStateAdapter extends FragmentStateAdapter {

		public MainNavigationStateAdapter(@NonNull FragmentActivity fragmentActivity) {
			super(fragmentActivity);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			switch (position) {
				case 0:
					return MainFragment.newInstance();
				case 1:
					return TheAppFragment.newInstance();
				default:
					throw new IllegalArgumentException("No fragment associated with given position: " + position);
			}
		}

		@Override
		public int getItemCount() {
			return 2;
		}

	}

}
