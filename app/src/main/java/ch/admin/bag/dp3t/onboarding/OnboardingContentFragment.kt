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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.databinding.FragmentOnboardingContentBinding
import ch.admin.bag.dp3t.databinding.ItemIconWithTextBinding

private const val ARG_RES_TITLE = "ARG_RES_TITLE"
private const val ARG_RES_SUBTITLE = "ARG_RES_SUBTITLE"
private const val ARG_RES_DESCRIPTIONS = "ARG_RES_DESCRIPTIONS"
private const val ARG_RES_DESCR_ICONS = "ARG_RES_DESCR_ICONS"
private const val ARG_RES_ILLUSTRATION = "ARG_RES_ILLUSTRATION"

class OnboardingContentFragment : Fragment() {

	companion object {

		fun newInstance(
			@StringRes title: Int,
			@StringRes subtitle: Int,
			@DrawableRes illustration: Int,
			@StringRes descriptions: IntArray,
			@DrawableRes icons: IntArray
		) = OnboardingContentFragment().apply {
			arguments = bundleOf(
				ARG_RES_TITLE to title,
				ARG_RES_SUBTITLE to subtitle,
				ARG_RES_ILLUSTRATION to illustration,
				ARG_RES_DESCRIPTIONS to descriptions,
				ARG_RES_DESCR_ICONS to icons
			)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentOnboardingContentBinding.inflate(inflater).apply {
			val args = requireArguments()
			onboardingTitle.setText(args.getInt(ARG_RES_TITLE))
			onboardingSubtitle.setText(args.getInt(ARG_RES_SUBTITLE))
			onboardingIllustration.setImageResource(args.getInt(ARG_RES_ILLUSTRATION))

			val descriptions = args.getIntArray(ARG_RES_DESCRIPTIONS) ?: intArrayOf()
			val icons = args.getIntArray(ARG_RES_DESCR_ICONS) ?: intArrayOf()
			for ((description, icon) in descriptions.zip(icons)) {
				ItemIconWithTextBinding.inflate(inflater, descriptionContainer, true).apply {
					this.icon.setImageResource(icon)
					this.text.setText(description)
				}
			}

			onboardingContinueButton.setOnClickListener { (activity as OnboardingActivity?)!!.continueToNextPage() }
		}.root
	}

}