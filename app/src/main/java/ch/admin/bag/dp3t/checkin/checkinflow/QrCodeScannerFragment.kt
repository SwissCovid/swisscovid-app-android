package ch.admin.bag.dp3t.checkin.checkinflow

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.BuildConfig
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState
import ch.admin.bag.dp3t.checkin.utils.ErrorDialog
import ch.admin.bag.dp3t.checkin.utils.ErrorHelper
import ch.admin.bag.dp3t.databinding.FragmentQrCodeScannerBinding
import ch.admin.bag.dp3t.extensions.showFragment
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.utils.QrUtils.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val PERMISSION_REQUEST_CAMERA = 13
private const val MIN_ERROR_VISIBILITY = 1000L

class QrCodeScannerFragment : Fragment(), QrCodeAnalyzer.Listener {


	companion object {
		val TAG = QrCodeScannerFragment::class.java.canonicalName

		@JvmStatic
		fun newInstance(): QrCodeScannerFragment = QrCodeScannerFragment()
	}


	private val viewModel: CrowdNotifierViewModel by activityViewModels()

	private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

	private var isQRScanningEnabled = true
	private var lastUIErrorUpdate = 0L

	private lateinit var binding: FragmentQrCodeScannerBinding

	override fun onResume() {
		isQRScanningEnabled = true
		super.onResume()
	}

	override fun onStart() {
		super.onStart()
		if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) !=
			PackageManager.PERMISSION_GRANTED
		) {
			val dialog = CameraPermissionExplanationDialog(requireContext())
			dialog.setOnCancelListener { refreshView(false) }
			dialog.setGrantCameraAccessClickListener {
				requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
			}
			dialog.show()
		} else {
			startCameraAndQrAnalyzer()
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentQrCodeScannerBinding.inflate(inflater)
		return binding.apply {
			fragmentQrScannerToolbar.setNavigationOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
		}.root
	}

	private fun setupFlashButton(camera: Camera) {
		binding.apply {
			flashButton.isVisible = camera.cameraInfo.hasFlashUnit()

			camera.cameraInfo.torchState.observe(viewLifecycleOwner, { v: Int ->
				if (v == TorchState.ON) {
					flashButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_light_on))
				} else {
					flashButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_light_off))
				}
			})
			flashButton.setOnClickListener { camera.cameraControl.enableTorch(camera.cameraInfo.torchState.value == TorchState.OFF) }
		}
	}

	private fun startCameraAndQrAnalyzer() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
		cameraProviderFuture.addListener({
			try {
				val cameraProvider = cameraProviderFuture.get()
				val preview = Preview.Builder().build()
				preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
				val imageAnalyzer = ImageAnalysis.Builder().build()
				imageAnalyzer.setAnalyzer(cameraExecutor, QrCodeAnalyzer(this))
				val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
				cameraProvider.unbindAll()
				val camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)
				setupFlashButton(camera)
			} catch (e: ExecutionException) {
				Log.d(TAG, "Error starting camera " + e.message)
				throw RuntimeException(e)
			} catch (e: InterruptedException) {
				Log.w("QR Analysis Interrupted", e.message!!)
				Thread.currentThread().interrupt()
			}
		}, ContextCompat.getMainExecutor(requireContext()))
	}

	override fun onDestroy() {
		super.onDestroy()
		cameraExecutor.shutdown()
	}

	override fun noQRCodeFound() {
		activity?.runOnUiThread { indicateInvalidQrCode(QRScannerState.NO_CODE_FOUND) }
	}

	@Synchronized
	override fun onQRCodeFound(qrCodeData: String) {
		if (!isQRScanningEnabled) return
		try {
			val venueInfo = CrowdNotifier.getVenueInfo(qrCodeData, BuildConfig.ENTRY_QR_CODE_HOST)
			isQRScanningEnabled = false
			activity?.runOnUiThread {
				viewModel.checkInState = CheckInState(false, venueInfo, System.currentTimeMillis(), System.currentTimeMillis(), 0)
			}
			showFragment(CheckInFragment.newInstance(isSelfCheckin = false))
			activity?.runOnUiThread { indicateInvalidQrCode(QRScannerState.VALID) }
		} catch (e: QRException) {
			handleInvalidQRCodeExceptions(e)
		}
	}

	private fun handleInvalidQRCodeExceptions(e: QRException) {
		when (e) {
			is InvalidQRCodeVersionException -> {
				activity?.runOnUiThread {
					isQRScanningEnabled = false
					val errorDialog = ErrorDialog(requireContext(), CrowdNotifierErrorState.UPDATE_REQUIRED)
					errorDialog.setOnDismissListener { isQRScanningEnabled = true }
					errorDialog.setOnCancelListener { isQRScanningEnabled = true }
					errorDialog.show()
				}
			}
			is NotYetValidException -> activity?.runOnUiThread { indicateInvalidQrCode(QRScannerState.NOT_YET_VALID) }
			is NotValidAnymoreException -> activity?.runOnUiThread { indicateInvalidQrCode(QRScannerState.NOT_VALID_ANYMORE) }
			else -> activity?.runOnUiThread { indicateInvalidQrCode(QRScannerState.INVALID_FORMAT) }

		}
	}

	private fun indicateInvalidQrCode(qrScannerState: QRScannerState) {
		binding.apply {
			val currentTime = System.currentTimeMillis()
			if (lastUIErrorUpdate > currentTime - MIN_ERROR_VISIBILITY && qrScannerState == QRScannerState.NO_CODE_FOUND) {
				return
			}
			lastUIErrorUpdate = currentTime
			var color = R.color.primary
			if (qrScannerState == QRScannerState.VALID || qrScannerState == QRScannerState.NO_CODE_FOUND) {
				qrCodeScannerInvalidCodeText.visibility = View.INVISIBLE
			} else {
				qrCodeScannerInvalidCodeText.visibility = View.VISIBLE
				color = R.color.status_red
			}
			when (qrScannerState) {
				QRScannerState.INVALID_FORMAT -> qrCodeScannerInvalidCodeText.setText(R.string.qrscanner_error)
				QRScannerState.NOT_VALID_ANYMORE -> qrCodeScannerInvalidCodeText.setText(R.string.qr_scanner_error_code_not_valid_anymore)
				QRScannerState.NOT_YET_VALID -> qrCodeScannerInvalidCodeText.setText(R.string.qr_scanner_error_code_not_yet_valid)
				else -> Unit
			}
			setIndicatorColor(qrCodeScannerTopLeftIndicator, color)
			setIndicatorColor(qrCodeScannerTopRightIndicator, color)
			setIndicatorColor(qrCodeScannerBottomLeftIndicator, color)
			setIndicatorColor(qrCodeScannerBottomRightIndicator, color)
		}
	}

	private fun setIndicatorColor(indicator: View, @ColorRes color: Int) {
		val drawable = indicator.background as LayerDrawable
		val stroke = drawable.findDrawableByLayerId(R.id.indicator) as GradientDrawable
		if (context == null) return
		stroke.setStroke(
			resources.getDimensionPixelSize(R.dimen.qr_scanner_indicator_stroke_width),
			resources.getColor(color, null)
		)
	}

	private fun refreshView(cameraPermissionGranted: Boolean) {
		binding.apply {
			fragmentQrScannerMainView.isVisible = cameraPermissionGranted
			fragmentQrScannerErrorView.isVisible = !cameraPermissionGranted

			if (cameraPermissionGranted) {
				startCameraAndQrAnalyzer()
			} else {
				ErrorHelper.updateErrorView(fragmentQrScannerErrorView, CrowdNotifierErrorState.CAMERA_ACCESS_DENIED, null, context)
			}
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		if (requestCode == PERMISSION_REQUEST_CAMERA) {
			refreshView(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		}
	}

	internal enum class QRScannerState {
		NO_CODE_FOUND, VALID, INVALID_FORMAT, NOT_YET_VALID, NOT_VALID_ANYMORE
	}

}