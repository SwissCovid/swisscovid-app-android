/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.onboarding;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.dpppt.android.sdk.DP3T;

import ch.admin.bag.dp3t.R;

public class OnboardingActivity extends FragmentActivity {

	private static final int SHOW_SPLASHBOARDING_MILLIS = 3000;

	private View splashboarding;
	private ViewPager2 viewPager;
	private FragmentStateAdapter pagerAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_onboarding);

		splashboarding = findViewById(R.id.splashboarding);
		viewPager = findViewById(R.id.pager);
		viewPager.setUserInputEnabled(false);
		pagerAdapter = new OnboardingSlidePageAdapter(this);
		viewPager.setAdapter(pagerAdapter);

		splashboarding
				.postDelayed(() -> {
					splashboarding.setAlpha(1);
					splashboarding.animate().alpha(0).setDuration(200).withLayer().setListener(new Animator.AnimatorListener() {
						@Override
						public void onAnimationStart(Animator animator) {}

						@Override
						public void onAnimationEnd(Animator animator) {
							splashboarding.setVisibility(View.GONE);
						}

						@Override
						public void onAnimationCancel(Animator animator) {}

						@Override
						public void onAnimationRepeat(Animator animator) {}
					}).start();
				}, SHOW_SPLASHBOARDING_MILLIS);
	}

	public void continueToNextPage() {
		int currentItem = viewPager.getCurrentItem();
		if (currentItem < pagerAdapter.getItemCount() - 1) {
			viewPager.setCurrentItem(currentItem + 1, true);
		} else {
			setResult(RESULT_OK);
			finish();
			overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_open_exit);
		}
	}

	public void abortOnboarding() {
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DP3T.onActivityResult(this, requestCode, resultCode, data);
	}

}
