package ch.admin.bag.dp3t.inform

import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.inform.models.Status
import ch.admin.bag.dp3t.networking.errors.InvalidCodeError
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.util.ENExceptionHelper
import ch.admin.bag.dp3t.util.showFragment
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import com.google.android.gms.common.api.ApiException
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.internal.logger.Logger
import java.util.concurrent.CancellationException

abstract class TraceKeyShareBaseFragment : Fragment() {


	companion object {
		private const val TAG = "TraceKeyShareBaseFragment"
	}

	protected val informViewModel: InformViewModel by activityViewModels()
	private val tracingViewModel: TracingViewModel by activityViewModels()

	abstract fun setLoadingViewVisible(isVisible: Boolean)
	abstract fun setSendButtonEnabled(isEnabled: Boolean)
	abstract fun setInvalidCodeErrorVisible(isVisible: Boolean)
	abstract fun performNotShareAction()

	protected fun authenticateInputAndInformExposed(authCode: String) {
		val isTracingEnabled = DP3T.isTracingEnabled(requireContext())
		if (!isTracingEnabled) {
			askUserToEnableTracing(authCode)
			return
		}

		informViewModel.authenticateInputAndGetDP3TAccessToken(authCode).observe(viewLifecycleOwner) {
			when (it.status) {
				Status.LOADING -> {
					setLoadingViewVisible(true)
					setSendButtonEnabled(false)
				}
				Status.ERROR -> {
					setLoadingViewVisible(false)
					it.exception?.let { exception ->
						handleAuthenticateRequestError(exception)
					}
					setSendButtonEnabled(true)
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
					setLoadingViewVisible(true)
					setSendButtonEnabled(false)
				}
				Status.ERROR -> {
					setLoadingViewVisible(false)
					it.exception?.let { exception ->
						handleInformExposedRequestError(exception)
					}
					setSendButtonEnabled(true)
				}
				Status.SUCCESS -> {
					setLoadingViewVisible(false)
					showFragment(ShareCheckinsFragment.newInstance(), R.id.inform_fragment_container)
					setSendButtonEnabled(true)
				}
			}
		}
	}

	private fun handleInformExposedRequestError(throwable: Throwable) {
		when (throwable) {
			is ResponseError -> showErrorDialog(InformRequestError.RED_STATUS_ERROR, throwable.statusCode.toString())
			is CancellationException -> performNotShareAction()
			is ApiException -> showErrorDialog(InformRequestError.RED_EXPOSURE_API_ERROR, throwable.statusCode.toString())
			else -> showErrorDialog(InformRequestError.RED_MISC_NETWORK_ERROR)
		}
		throwable.printStackTrace()
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

	private fun askUserToEnableTracing(authCode: String) {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.android_inform_tracing_enabled_explanation)
			.setOnCancelListener { setSendButtonEnabled(true) }
			.setNegativeButton(R.string.cancel) { _, _ -> setSendButtonEnabled(true) }
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
					.setOnDismissListener { setSendButtonEnabled(true) }
					.setPositiveButton(R.string.android_button_ok) { _, _ -> }
					.show()
			}
		) { setSendButtonEnabled(true) }
	}

}