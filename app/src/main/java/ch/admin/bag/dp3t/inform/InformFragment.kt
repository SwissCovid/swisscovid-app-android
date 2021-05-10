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
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentInformBinding
import ch.admin.bag.dp3t.inform.views.ChainedEditText
import ch.admin.bag.dp3t.inform.views.ChainedEditText.ChainedEditTextListener
import ch.admin.bag.dp3t.util.PhoneUtil
import ch.admin.bag.dp3t.util.showFragment

private const val REGEX_CODE_PATTERN = "\\d{" + ChainedEditText.NUM_CHARACTERS + "}"

class InformFragment : TraceKeyShareBaseFragment() {

	private lateinit var binding: FragmentInformBinding

	companion object {
		private const val TAG = "InformFragment"

		@JvmStatic
		fun newInstance() = InformFragment()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentInformBinding.inflate(inflater).apply {
			(requireActivity() as InformActivity).allowBackButton(true)
			covidcodeInput.addTextChangedListener(object : ChainedEditTextListener {
				override fun onTextChanged(input: String) {
					sendButton.isEnabled = input.matches(REGEX_CODE_PATTERN.toRegex())
				}

				override fun onEditorSendAction() {
					if (sendButton.isEnabled) sendButton.callOnClick()
				}
			})

			informViewModel.getLastAuthCode()?.let {
				covidcodeInput.text = it
			}

			if (requireActivity().intent.extras != null) {
				val covidCode = requireActivity().intent.extras?.getString(InformActivity.EXTRA_COVIDCODE)
				if (covidCode != null) {
					covidcodeInput.text = covidCode
				}
			}
			sendButton.setOnClickListener {
				sendButton.isEnabled = false
				setInvalidCodeErrorVisible(false)
				val authCode = covidcodeInput.text
				authenticateInputAndInformExposed(authCode)
			}
			cancelButton.setOnClickListener { requireActivity().finish() }
			informInvalidCodeError.setOnClickListener { PhoneUtil.callAppHotline(it.context) }
		}

		return binding.root
	}


	override fun onResume() {
		super.onResume()
		binding.covidcodeInput.requestFocus()
	}


	override fun setLoadingViewVisible(isVisible: Boolean) {
		binding.loadingView.isVisible = isVisible
	}

	override fun setSendButtonEnabled(isEnabled: Boolean) {
		binding.sendButton.isEnabled = isEnabled
	}

	override fun setInvalidCodeErrorVisible(visible: Boolean) {
		binding.apply {
			informInvalidCodeError.isVisible = visible
			informInputText.isVisible = !visible
		}
	}

	override fun performNotShareAction() {
		showFragment(ReallyNotShareFragment.newInstance(), R.id.inform_fragment_container)
	}

}