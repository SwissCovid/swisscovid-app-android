/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.vaccination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentVaccinationAppointmentBinding
import ch.admin.bag.dp3t.networking.models.VaccinationBookingInfoModel
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.UrlUtil

class VaccinationAppointmentFragment : Fragment() {

	companion object {
		@JvmStatic
		fun newInstance() = VaccinationAppointmentFragment()
	}

	private var _binding: FragmentVaccinationAppointmentBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentVaccinationAppointmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

		setupMoreInformationButton()

		val secureStorage = SecureStorage.getInstance(context)
		secureStorage.getVaccinationBookingInfo(requireContext().getString(R.string.language_key))?.let {
			setupVaccinationBookingInfo(it)
		}

	}


	private fun setupVaccinationBookingInfo(vaccinationBookingInfo: VaccinationBookingInfoModel) {
		binding.vaccinationBookingTitle.text = vaccinationBookingInfo.title
		binding.vaccinationBookingText.text = vaccinationBookingInfo.text
		binding.vaccinationBookingInfo.text = vaccinationBookingInfo.info

		if (vaccinationBookingInfo.impfcheckTitle != null && vaccinationBookingInfo.impfcheckText != null && vaccinationBookingInfo.impfcheckButton != null && vaccinationBookingInfo.impfcheckUrl != null) {
			binding.impfcheckTitle.text = vaccinationBookingInfo.impfcheckTitle
			binding.impfcheckInfoText.text = vaccinationBookingInfo.impfcheckText
			binding.impfcheckAction.text = vaccinationBookingInfo.impfcheckButton
			binding.impfcheckAction.setOnClickListener {
				UrlUtil.openUrl(it.context, vaccinationBookingInfo.impfcheckUrl)
			}
		} else {
			binding.impfcheckTitle.visibility = View.GONE
			binding.impfcheckInfoText.visibility = View.GONE
			binding.impfcheckAction.visibility = View.GONE
		}
	}

	private fun setupMoreInformationButton() {
		binding.vaccinationMoreInfoButton.setOnClickListener {
			val url = getString(R.string.vaccination_booking_info_url)
			UrlUtil.openUrl(requireContext(), url)
		}
	}

}