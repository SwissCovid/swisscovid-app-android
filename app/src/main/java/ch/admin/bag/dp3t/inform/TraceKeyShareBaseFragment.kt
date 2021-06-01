package ch.admin.bag.dp3t.inform

import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.inform.models.Resource
import ch.admin.bag.dp3t.inform.models.Status
import ch.admin.bag.dp3t.networking.errors.InvalidCodeError
import ch.admin.bag.dp3t.networking.errors.ResponseError
import ch.admin.bag.dp3t.util.ENExceptionHelper
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import com.google.android.gms.common.api.ApiException
import org.dpppt.android.sdk.DP3T
import org.dpppt.android.sdk.PendingUploadTask
import org.dpppt.android.sdk.backend.ResponseCallback
import org.dpppt.android.sdk.internal.logger.Logger

abstract class TraceKeyShareBaseFragment : Fragment() {


	companion object {
		private const val TAG = "TraceKeyShareBaseFragment"
	}

	protected val informViewModel: InformViewModel by activityViewModels()
	private val tracingViewModel: TracingViewModel by activityViewModels()

	abstract fun setLoadingViewVisible(isVisible: Boolean)

	protected fun showShareTEKsPopup(onSuccess: () -> Unit, onError: () -> Unit) {
		DP3T.showShareTEKsPopup(requireActivity(), object : ResponseCallback<PendingUploadTask> {
			override fun onSuccess(pendingUploadTask: PendingUploadTask) {
				informViewModel.pendingUploadTask = pendingUploadTask
				onSuccess()
			}

			override fun onError(p0: Throwable?) {
				onError()
			}
		})
	}

	protected fun askUserToEnableTracingIfNecessary(continuation: (isEnabled: Boolean) -> Unit) {
		val isTracingEnabled = DP3T.isTracingEnabled(requireContext())
		if (!isTracingEnabled) {
			showEnableTracingDialog(continuation)
		} else {
			continuation(true)
		}
	}

	protected fun performUpload(onSuccess: () -> Unit) {
		informViewModel.performUpload().observe(viewLifecycleOwner) {
			when (it.status) {
				Status.LOADING -> {
					setLoadingViewVisible(true)
				}
				Status.ERROR -> {
					setLoadingViewVisible(false)
					handleUploadException(it)
				}
				Status.SUCCESS -> {
					setLoadingViewVisible(false)
					onSuccess()
				}
			}
		}
	}

	private fun handleUploadException(error: Resource<InformRequestError?>) {
		if (error.data == null) return
		when (error.exception) {
			is ResponseError -> showErrorDialog(error.data, error.exception.statusCode.toString())
			is ApiException -> showErrorDialog(error.data, error.exception.statusCode.toString())
			is InvalidCodeError -> showErrorDialog(error.data)
			else -> showErrorDialog(error.data)
		}
		error.exception?.printStackTrace()
	}

	protected fun showErrorDialog(error: InformRequestError, addErrorCode: String? = null) {
		val errorDialogBuilder = AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(error.errorMessage)
			.setPositiveButton(R.string.android_button_ok) { _, _ -> }
		val errorCode = error.getErrorCode(addErrorCode)
		val errorCodeView = layoutInflater.inflate(R.layout.view_dialog_error_code, view as ViewGroup?, false) as TextView
		errorCodeView.text = errorCode
		errorDialogBuilder.setView(errorCodeView)
		errorDialogBuilder.show()
	}

	private fun showEnableTracingDialog(continuation: (isEnabled: Boolean) -> Unit) {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.android_inform_tracing_enabled_explanation)
			.setOnCancelListener { continuation(false) }
			.setNegativeButton(R.string.cancel) { _, _ -> continuation(false) }
			.setPositiveButton(R.string.activate_tracing_button) { _, _ -> enableTracing(continuation) }
			.show()
	}

	private fun enableTracing(continuation: (isEnabled: Boolean) -> Unit) {
		tracingViewModel.enableTracing(requireActivity(),
			{ continuation(true) },
			{ e: Exception? ->
				val message = ENExceptionHelper.getErrorMessage(e, activity)
				Logger.e(TAG, message)
				AlertDialog.Builder(requireActivity(), R.style.NextStep_AlertDialogStyle)
					.setTitle(R.string.android_en_start_failure)
					.setMessage(message)
					.setOnDismissListener { continuation(false) }
					.setPositiveButton(R.string.android_button_ok) { _, _ -> }
					.show()
			}
		) { continuation(false) }
	}

}