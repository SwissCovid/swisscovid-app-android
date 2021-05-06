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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentInformBinding
import ch.admin.bag.dp3t.inform.models.Status
import ch.admin.bag.dp3t.inform.views.ChainedEditText
import ch.admin.bag.dp3t.inform.views.ChainedEditText.ChainedEditTextListener
import ch.admin.bag.dp3t.networking.errors.InvalidCodeError
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.util.ENExceptionHelper
import ch.admin.bag.dp3t.util.PhoneUtil
import ch.admin.bag.dp3t.util.showFragment
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import com.google.android.gms.common.api.ApiException
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.internal.logger.Logger
import java.util.concurrent.CancellationException

private const val REGEX_CODE_PATTERN = "\\d{" + ChainedEditText.NUM_CHARACTERS + "}"

class InformFragment : Fragment() {

	private val tracingViewModel: TracingViewModel by activityViewModels()
	private val informViewModel: InformViewModel by activityViewModels()
	private lateinit var progressDialog: AlertDialog

	private lateinit var binding: FragmentInformBinding

	companion object {
		private const val TAG = "InformFragment"

		@JvmStatic
		fun newInstance() = InformFragment()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentInformBinding.inflate(inflater).apply {
			(requireActivity() as InformActivity).allowBackButton(true)
			progressDialog = AlertDialog.Builder(requireContext()).setView(R.layout.dialog_loading).create()
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
				val isTracingEnabled = DP3T.isTracingEnabled(requireContext())
				if (isTracingEnabled) {
					authenticateInputAndInformExposed(authCode)
				} else {
					askUserToEnableTracing(authCode)
				}
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

	private fun authenticateInputAndInformExposed(authCode: String) {
		informViewModel.authenticateInputAndGetDP3TAccessToken(authCode).observe(viewLifecycleOwner) {
			when (it.status) {
				Status.LOADING -> {
					setProgressDialogVisible(true)
					binding.sendButton.isEnabled = false
				}
				Status.ERROR -> {
					setProgressDialogVisible(false)
					it.exception?.let { exception ->
						handleAuthenticateRequestError(exception)
					}
					binding.sendButton.isEnabled = true
				}
				Status.SUCCESS -> {
					it.data?.let { accessToken -> informExposed(accessToken) }
				}
			}
		}
	}

	private fun handleAuthenticateRequestError(throwable: Throwable) {
		when (throwable) {
			is InvalidCodeError -> setInvalidCodeErrorVisible(true)
			is ResponseError -> showErrorDialog(InformRequestError.BLACK_STATUS_ERROR, throwable.statusCode.toString())
			else -> showErrorDialog(InformRequestError.BLACK_MISC_NETWORK_ERROR)
		}
		throwable.printStackTrace()
	}

	private fun informExposed(accessToken: String) {
		informViewModel.informExposed(accessToken, requireActivity()).observe(viewLifecycleOwner) {
			when (it.status) {
				Status.LOADING -> {
					setProgressDialogVisible(true)
					binding.sendButton.isEnabled = false
				}
				Status.ERROR -> {
					setProgressDialogVisible(false)
					it.exception?.let { exception ->
						handleInformExposedRequestError(exception)
					}
					binding.sendButton.isEnabled = true
				}
				Status.SUCCESS -> {
					setProgressDialogVisible(false)
					showFragment(ShareCheckinsFragment.newInstance(), R.id.inform_fragment_container)
					binding.sendButton.isEnabled = true
				}
			}
		}
	}

	private fun handleInformExposedRequestError(throwable: Throwable) {
		when (throwable) {
			is ResponseError -> showErrorDialog(InformRequestError.RED_STATUS_ERROR, throwable.statusCode.toString())
			is CancellationException -> showFragment(ReallyNotShareFragment.newInstance(), R.id.inform_fragment_container)
			is ApiException -> showErrorDialog(InformRequestError.RED_EXPOSURE_API_ERROR, throwable.statusCode.toString())
			else -> showErrorDialog(InformRequestError.RED_MISC_NETWORK_ERROR)
		}
		throwable.printStackTrace()
	}

	private fun setProgressDialogVisible(isVisible: Boolean) {
		if (isVisible && !progressDialog.isShowing) {
			progressDialog.show()
		}
		if (!isVisible && progressDialog.isShowing) {
			progressDialog.dismiss()
		}
	}

	private fun askUserToEnableTracing(authCode: String) {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.android_inform_tracing_enabled_explanation)
			.setOnCancelListener { binding.sendButton.isEnabled = true }
			.setNegativeButton(R.string.cancel) { _, _ -> binding.sendButton.isEnabled = true }
			.setPositiveButton(R.string.activate_tracing_button) { _, _ -> enableTracing(authCode) }
			.show()
	}

	private fun enableTracing(authCode: String) {
		tracingViewModel.enableTracing(requireActivity(),
			{ authenticateInputAndInformExposed(authCode) },
			{ e: Exception? ->
				val message = ENExceptionHelper.getErrorMessage(e, activity)
				Logger.e(TAG, message)
				AlertDialog.Builder(requireActivity(), R.style.NextStep_AlertDialogStyle)
					.setTitle(R.string.android_en_start_failure)
					.setMessage(message)
					.setOnDismissListener { binding.sendButton.isEnabled = true }
					.setPositiveButton(R.string.android_button_ok) { _, _ -> }
					.show()
			}
		) { binding.sendButton.isEnabled = true }
	}

	private fun setInvalidCodeErrorVisible(visible: Boolean) {
		binding.apply {
			informInvalidCodeError.isVisible = visible
			informInputText.isVisible = visible
		}
	}

	private fun showErrorDialog(error: InformRequestError, addErrorCode: String? = null) {
		val errorDialogBuilder = AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(error.errorMessage)
			.setPositiveButton(R.string.android_button_ok) { _, _ -> }
		val errorCode = error.getErrorCode(addErrorCode)
		val errorCodeView = layoutInflater.inflate(R.layout.view_dialog_error_code, view as ViewGroup?, false) as TextView
		errorCodeView.text = errorCode
		errorDialogBuilder.setView(errorCodeView)
		errorDialogBuilder.show()
	}

}