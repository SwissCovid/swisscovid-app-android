package ch.admin.bag.dp3t.checkin.checkinflow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListenableFuture;

import org.crowdnotifier.android.sdk.CrowdNotifier;
import org.crowdnotifier.android.sdk.model.VenueInfo;
import org.crowdnotifier.android.sdk.utils.QrUtils;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel;
import ch.admin.bag.dp3t.checkin.models.CheckInState;
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState;
import ch.admin.bag.dp3t.checkin.utils.ErrorDialog;
import ch.admin.bag.dp3t.checkin.utils.ErrorHelper;

public class QrCodeScannerFragment extends Fragment implements QrCodeAnalyzer.Listener {

	public final static String TAG = QrCodeScannerFragment.class.getCanonicalName();
	private static final int PERMISSION_REQUEST_CAMERA = 13;
	private static final long MIN_ERROR_VISIBILITY = 1000L;

	private CrowdNotifierViewModel viewModel;
	private PreviewView previewView;
	private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
	private ExecutorService cameraExecutor;
	private ImageButton flashButton;
	private TextView invalidCodeText;
	private View topLeftIndicator;
	private View topRightIndicator;
	private View bottomRightIndicator;
	private View bottomLeftIndicator;
	private View errorView;
	private View mainView;
	private boolean goToHome = false;
	private boolean isQRScanningEnabled = true;
	private long lastUIErrorUpdate = 0L;

	public QrCodeScannerFragment() { super(R.layout.fragment_qr_code_scanner); }

	public static QrCodeScannerFragment newInstance() {
		return new QrCodeScannerFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		viewModel = new ViewModelProvider(requireActivity()).get(CrowdNotifierViewModel.class);
		cameraExecutor = Executors.newSingleThreadExecutor();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		if (goToHome) {
			requireActivity().getSupportFragmentManager().popBackStack();
			goToHome = false;
		} else {
			isQRScanningEnabled = true;
		}
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) !=
				PackageManager.PERMISSION_GRANTED) {
			CameraPermissionExplanationDialog dialog = new CameraPermissionExplanationDialog(requireContext());
			dialog.setOnCancelListener(v -> refreshView(false));
			dialog.setGrantCameraAccessClickListener(
					v -> requestPermissions(new String[] { Manifest.permission.CAMERA }, PERMISSION_REQUEST_CAMERA));
			dialog.show();
		} else {
			startCameraAndQrAnalyzer();
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		previewView = view.findViewById(R.id.camera_preview);
		flashButton = view.findViewById(R.id.fragment_qr_scanner_flash_button);
		invalidCodeText = view.findViewById(R.id.qr_code_scanner_invalid_code_text);
		topLeftIndicator = view.findViewById(R.id.qr_code_scanner_top_left_indicator);
		topRightIndicator = view.findViewById(R.id.qr_code_scanner_top_right_indicator);
		bottomLeftIndicator = view.findViewById(R.id.qr_code_scanner_bottom_left_indicator);
		bottomRightIndicator = view.findViewById(R.id.qr_code_scanner_bottom_right_indicator);
		errorView = view.findViewById(R.id.fragment_qr_scanner_error_view);
		mainView = view.findViewById(R.id.fragment_qr_scanner_main_view);
		Toolbar toolbar = view.findViewById(R.id.fragment_qr_scanner_toolbar);

		toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
	}


	private void setupFlashButton(Camera camera) {

		if (!camera.getCameraInfo().hasFlashUnit()) {
			flashButton.setVisibility(View.GONE);
		} else {
			flashButton.setVisibility(View.VISIBLE);
		}

		camera.getCameraInfo().getTorchState().observe(getViewLifecycleOwner(), v -> {
			if (v == TorchState.ON) {
				flashButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_light_off));
			} else {
				flashButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_light_on));
			}
		});

		flashButton.setOnClickListener(v -> {
			camera.getCameraControl().enableTorch(camera.getCameraInfo().getTorchState().getValue() == TorchState.OFF);
		});
	}

	private void startCameraAndQrAnalyzer() {
		cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
		cameraProviderFuture.addListener(() -> {
			try {
				ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
				Preview preview = new Preview.Builder().build();
				preview.setSurfaceProvider(previewView.getSurfaceProvider());

				ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder().build();
				imageAnalyzer.setAnalyzer(cameraExecutor, new QrCodeAnalyzer(this));

				CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

				cameraProvider.unbindAll();
				Camera camera = cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalyzer);
				setupFlashButton(camera);
			} catch (ExecutionException e) {
				Log.d(TAG, "Error starting camera " + e.getMessage());
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				Log.w("QR Analysis Interrupted", e.getMessage());
				Thread.currentThread().interrupt();
			}
		}, ContextCompat.getMainExecutor(requireContext()));
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		cameraExecutor.shutdown();
	}

	@Override
	public void noQRCodeFound() {
		if (getActivity() != null) getActivity().runOnUiThread(() -> indicateInvalidQrCode(QRScannerState.NO_CODE_FOUND));
	}

	@Override
	public synchronized void onQRCodeFound(String qrCodeData) {
		if (!isQRScanningEnabled) return;
		try {
			VenueInfo venueInfo = CrowdNotifier.getVenueInfo(qrCodeData, BuildConfig.ENTRY_QR_CODE_PREFIX);
			isQRScanningEnabled = false;
			if (getActivity() != null) getActivity().runOnUiThread(() -> viewModel.setCheckInState(
					new CheckInState(false, venueInfo, System.currentTimeMillis(), System.currentTimeMillis(), 0)));
			showCheckInFragment();
			if (getActivity() != null) getActivity().runOnUiThread(() -> indicateInvalidQrCode(QRScannerState.VALID));
		} catch (QrUtils.QRException e) {
			handleInvalidQRCodeExceptions(qrCodeData, e);
		}
	}

	private void handleInvalidQRCodeExceptions(String qrCodeData, QrUtils.QRException e) {
		if (e instanceof QrUtils.InvalidQRCodeVersionException) {
			if (getActivity() != null) getActivity().runOnUiThread(() -> {
				isQRScanningEnabled = false;
				ErrorDialog errorDialog = new ErrorDialog(requireContext(), CrowdNotifierErrorState.UPDATE_REQUIRED);
				errorDialog.setOnDismissListener(v -> isQRScanningEnabled = true);
				errorDialog.setOnCancelListener(v -> isQRScanningEnabled = true);
				errorDialog.show();
			});
		} else if (e instanceof QrUtils.NotYetValidException) {
			if (getActivity() != null) getActivity().runOnUiThread(() -> indicateInvalidQrCode(QRScannerState.NOT_YET_VALID));
		} else if (e instanceof QrUtils.NotValidAnymoreException) {
			if (getActivity() != null) getActivity().runOnUiThread(() -> indicateInvalidQrCode(QRScannerState.NOT_VALID_ANYMORE));
		} else {
			if (qrCodeData.startsWith(BuildConfig.TRACE_QR_CODE_PREFIX)) {
				isQRScanningEnabled = false;
				Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrCodeData));
				startActivity(openBrowserIntent);
				goToHome = true;
			} else {
				if (getActivity() != null) getActivity().runOnUiThread(() -> indicateInvalidQrCode(QRScannerState.INVALID_FORMAT));
			}
		}
	}

	private void indicateInvalidQrCode(QRScannerState qrScannerState) {
		long currentTime = System.currentTimeMillis();
		if (lastUIErrorUpdate > currentTime - MIN_ERROR_VISIBILITY && qrScannerState == QRScannerState.NO_CODE_FOUND) {
			return;
		}
		lastUIErrorUpdate = currentTime;
		int color = R.color.primary;
		if (qrScannerState == QRScannerState.VALID || qrScannerState == QRScannerState.NO_CODE_FOUND) {
			invalidCodeText.setVisibility(View.INVISIBLE);
		} else {
			invalidCodeText.setVisibility(View.VISIBLE);
			color = R.color.tertiary;
		}

		if (qrScannerState == QRScannerState.INVALID_FORMAT) {
			invalidCodeText.setText(R.string.qrscanner_error);
		} else if (qrScannerState == QRScannerState.NOT_VALID_ANYMORE) {
			invalidCodeText.setText(R.string.qr_scanner_error_code_not_valid_anymore);
		} else if (qrScannerState == QRScannerState.NOT_YET_VALID) {
			invalidCodeText.setText(R.string.qr_scanner_error_code_not_yet_valid);
		}
		setIndicatorColor(topLeftIndicator, color);
		setIndicatorColor(topRightIndicator, color);
		setIndicatorColor(bottomLeftIndicator, color);
		setIndicatorColor(bottomRightIndicator, color);
	}

	private void setIndicatorColor(View indicator, @ColorRes int color) {
		LayerDrawable drawable = (LayerDrawable) indicator.getBackground();
		GradientDrawable stroke = (GradientDrawable) drawable.findDrawableByLayerId(R.id.indicator);
		if (getContext() == null) return;
		stroke.setStroke(getResources().getDimensionPixelSize(R.dimen.qr_scanner_indicator_stroke_width),
				getResources().getColor(color, null));
	}

	private void showCheckInFragment() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, CheckInFragment.newInstance(false))
				.addToBackStack(CheckInFragment.class.getCanonicalName())
				.commitAllowingStateLoss();
	}

	private void refreshView(boolean cameraPermissionGranted) {
		if (cameraPermissionGranted) {
			mainView.setVisibility(View.VISIBLE);
			errorView.setVisibility(View.GONE);
			startCameraAndQrAnalyzer();
		} else {
			errorView.setVisibility(View.VISIBLE);
			ErrorHelper.updateErrorView(errorView, CrowdNotifierErrorState.CAMERA_ACCESS_DENIED, null, getContext());
			mainView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CAMERA) {
			refreshView(grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
		}
	}

	enum QRScannerState {
		NO_CODE_FOUND, VALID, INVALID_FORMAT, NOT_YET_VALID, NOT_VALID_ANYMORE
	}

}
