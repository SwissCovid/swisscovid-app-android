/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ch.admin.bag.dp3t.R

class OnboardingSlidePageAdapter(fragmentActivity: FragmentActivity, onboardingType: OnboardingType) :
	FragmentStateAdapter(fragmentActivity) {

	companion object {
		// Increment this number for each new Update Boarding
		const val UPDATE_BOARDING_VERSION = 2
	}

	private val onboardingScreens = when (onboardingType) {
		OnboardingType.UPDATE_BOARDING -> listOf<Fragment>(UpdateBoardingFragment.newInstance())
		OnboardingType.INSTANT_PART -> listOf<Fragment>(OnboardingDisclaimerFragment.newInstance())
		OnboardingType.NON_INSTANT_PART -> listOf<Fragment>(
			OnboardingContentFragment.newInstance(
				R.string.onboarding_begegnungen_title,
				R.string.onboarding_begegnungen_heading,
				R.drawable.ill_bluetooth,
				intArrayOf(
					R.string.onboarding_begegnungen_text1,
					R.string.onboarding_begegnungen_text2,
					R.string.onboarding_privacy_text1
				),
				intArrayOf(R.drawable.ic_begegnungen, R.drawable.ic_bluetooth, R.drawable.ic_key)
			),
			OnboardingBatteryPermissionFragment.newInstance(),
			OnboardingGaenPermissionFragment.newInstance(onboardingType),
			OnboardingFinishedFragment.newInstance(onboardingType)
		)
		OnboardingType.NORMAL -> listOf<Fragment>(
			OnboardingContentFragment.newInstance(
				R.string.onboarding_prinzip_title,
				R.string.onboarding_prinzip_heading,
				R.drawable.ill_prinzip,
				intArrayOf(R.string.onboarding_prinzip_text1, R.string.onboarding_prinzip_text2),
				intArrayOf(R.drawable.ic_begegnungen, R.drawable.ic_message_alert)
			),
			OnboardingContentFragment.newInstance(
				R.string.onboarding_checkin_title,
				R.string.onboarding_checkin_heading,
				R.drawable.ill_checkins,
				intArrayOf(R.string.onboarding_checkin_text1, R.string.onboarding_checkin_text2),
				intArrayOf(R.drawable.ic_qr, R.drawable.ic_info)
			),
			OnboardingContentFragment.newInstance(
				R.string.onboarding_begegnungen_title,
				R.string.onboarding_begegnungen_heading,
				R.drawable.ill_bluetooth,
				intArrayOf(
					R.string.onboarding_begegnungen_text1,
					R.string.onboarding_begegnungen_text2,
					R.string.onboarding_privacy_text1
				),
				intArrayOf(R.drawable.ic_begegnungen, R.drawable.ic_bluetooth, R.drawable.ic_key)
			),
			OnboardingDisclaimerFragment.newInstance(),
			OnboardingBatteryPermissionFragment.newInstance(),
			OnboardingGaenPermissionFragment.newInstance(onboardingType),
			OnboardingFinishedFragment.newInstance(onboardingType)
		)
	}


	override fun createFragment(position: Int): Fragment {
		return onboardingScreens[position]
	}

	override fun getItemCount(): Int {
		return onboardingScreens.size
	}
}