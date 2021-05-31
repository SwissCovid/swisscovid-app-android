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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentOnboardingFinishedBinding

private const val ARG_ONBOARDING_TYPE = "ARG_ONBOARDING_TYPE"

class OnboardingFinishedFragment : Fragment() {

	companion object {
		fun newInstance(onboardingType: OnboardingType) = OnboardingFinishedFragment().apply {
			arguments = bundleOf(ARG_ONBOARDING_TYPE to onboardingType)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentOnboardingFinishedBinding.inflate(inflater).apply {
			val onboardingType = requireArguments().getSerializable(ARG_ONBOARDING_TYPE) as OnboardingType
			if (onboardingType == OnboardingType.NON_INSTANT_PART) {
				onboardingTitle.setText(R.string.partial_onboarding_done_title)
				onboardingText.setText(R.string.partial_onboarding_done_text)
				onboardingContinueButton.setText(R.string.partial_onboarding_box_action)
			}
			onboardingContinueButton.setOnClickListener { (activity as OnboardingActivity).continueToNextPage() }
		}.root
	}

}