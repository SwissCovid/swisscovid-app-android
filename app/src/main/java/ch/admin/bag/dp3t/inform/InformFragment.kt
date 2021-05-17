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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentInformBinding
import ch.admin.bag.dp3t.inform.views.ChainedEditText
import ch.admin.bag.dp3t.inform.views.ChainedEditText.ChainedEditTextListener
import ch.admin.bag.dp3t.util.PhoneUtil
import ch.admin.bag.dp3t.util.showFragment

private const val REGEX_CODE_PATTERN = "\\d{" + ChainedEditText.NUM_CHARACTERS + "}"
private const val ARG_IS_COVID_CODE_INVALID_CASE = "ARG_IS_COVID_CODE_INVALID_CASE"

class InformFragment : TraceKeyShareBaseFragment() {

	companion object {
		private const val TAG = "InformFragment"

		@JvmStatic
		fun newInstance(isCovidCodeInvalidCase: Boolean = false) = InformFragment().apply {
			arguments = bundleOf(ARG_IS_COVID_CODE_INVALID_CASE to isCovidCodeInvalidCase)
		}
	}

	private lateinit var binding: FragmentInformBinding

	override fun onResume() {
		super.onResume()
		binding.covidcodeInput.requestFocus()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentInformBinding.inflate(inflater)
		return binding.apply {
			(requireActivity() as InformActivity).allowBackButton(true)
			covidcodeInput.addTextChangedListener(object : ChainedEditTextListener {
				override fun onTextChanged(input: String) {
					val matchesRegex = input.matches(REGEX_CODE_PATTERN.toRegex())
					val matchesChecksum = input.matchesChecksum()

					sendButton.isEnabled = matchesRegex && matchesChecksum
					setInvalidCovidcodeErrorVisible(matchesRegex && !matchesChecksum)
				}

				override fun onEditorSendAction() {
					if (sendButton.isEnabled) sendButton.callOnClick()
				}
			})

			covidcodeInput.text = informViewModel.covidCode

			if (requireActivity().intent.extras != null) {
				val covidCode = requireActivity().intent.extras?.getString(InformActivity.EXTRA_COVIDCODE)
				if (covidCode != null) {
					covidcodeInput.text = covidCode
				}
			}

			if (arguments?.getBoolean(ARG_IS_COVID_CODE_INVALID_CASE) == true) {
				binding.sendButton.setText(R.string.inform_send_button_title)
				setInvalidCovidcodeErrorVisible(true)
			}

			sendButton.setOnClickListener { performContinueAction() }
			cancelButton.setOnClickListener { requireActivity().finish() }
			informInvalidCodeError.setOnClickListener { PhoneUtil.callAppHotline(it.context) }
		}.root
	}

	private fun performContinueAction() {
		binding.sendButton.isEnabled = false
		setInvalidCovidcodeErrorVisible(false)
		informViewModel.covidCode = binding.covidcodeInput.text
		askUserToEnableTracingIfNecessary { tracingEnabled ->
			if (tracingEnabled) {
				if (arguments?.getBoolean(ARG_IS_COVID_CODE_INVALID_CASE) == true) {
					performUpload()
				} else {
					showShareTEKsPopup(onSuccess = ::onUserGrantedTEKSharing, onError = ::onUserDidNotGrantTEKSharing)
				}
			} else {
				binding.sendButton.isEnabled = true
			}
		}
	}

	private fun onUserGrantedTEKSharing() {
		informViewModel.hasSharedDP3TKeys = true
		if (informViewModel.selectableCheckinItems.isEmpty()) {
			performUpload()
		} else {
			showFragment(ShareCheckinsFragment.newInstance(), R.id.inform_fragment_container)
		}
	}

	private fun performUpload() {
		performUpload(onSuccess = {
			showFragment(ThankYouFragment.newInstance(), R.id.inform_fragment_container)
		}, onInvalidCovidCode = {
			setInvalidCovidcodeErrorVisible(true)
		})
	}

	private fun onUserDidNotGrantTEKSharing() {
		showFragment(ReallyNotShareFragment.newInstance(), R.id.inform_fragment_container)
	}

	private fun String.matchesChecksum(): Boolean {
		//TODO: Implement checksum check
		return true
	}

	override fun setLoadingViewVisible(isVisible: Boolean) {
		binding.loadingView.isVisible = isVisible
		binding.sendButton.isEnabled = !isVisible
	}

	private fun setInvalidCovidcodeErrorVisible(isVisible: Boolean) {
		binding.apply {
			informInvalidCodeError.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
			informInputText.visibility = if (!isVisible) View.VISIBLE else View.INVISIBLE
		}
	}

}