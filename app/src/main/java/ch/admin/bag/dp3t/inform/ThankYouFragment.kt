/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.inform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentThankYouBinding
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.DateUtils
import ch.admin.bag.dp3t.extensions.showFragment
import java.util.*

class ThankYouFragment : Fragment() {

	companion object {
		fun newInstance() = ThankYouFragment()
	}

	private val informViewModel: InformViewModel by activityViewModels()

	private val secureStorage by lazy { SecureStorage.getInstance(requireContext()) }

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentThankYouBinding.inflate(inflater).apply {
			(requireActivity() as InformActivity).allowBackButton(false)

			informThankYouTextCheckins.isVisible = informViewModel.hasSharedCheckins
			informThankYouTextInfo.isVisible = informViewModel.hasSharedDP3TKeys
			informThankYouTextOnsetdate.isVisible = informViewModel.hasSharedDP3TKeys

			// Show the onset date in the thank you message
			val oldestSharedKeyDateMillis = secureStorage.positiveReportOldestSharedKey
			if (oldestSharedKeyDateMillis > 0L) {
				val formattedDate = DateUtils.getFormattedDateWrittenMonth(oldestSharedKeyDateMillis, TimeZone.getTimeZone("UTC"))
				val formattedOnsetDateText =
					getString(R.string.inform_send_thankyou_text_onsetdate).replace("{ONSET_DATE}", formattedDate)
				informThankYouTextInfo.setText(R.string.inform_send_thankyou_text_onsetdate_info)
				informThankYouTextOnsetdate.text = formattedOnsetDateText
			} else {
				informThankYouTextInfo.setText(R.string.inform_send_thankyou_text)
				informThankYouTextOnsetdate.visibility = View.GONE
				informThankYouTextStopInfectionChains.visibility = View.GONE
			}

			informThankYouButtonContinue.setOnClickListener {
				showFragment(TracingStoppedFragment.newInstance(), R.id.inform_fragment_container)
			}
		}.root
	}

}