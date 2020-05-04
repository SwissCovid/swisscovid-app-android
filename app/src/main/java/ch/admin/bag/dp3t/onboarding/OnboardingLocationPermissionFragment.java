/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.onboarding;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.onboarding.util.PermissionButtonUtil;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.DeviceFeatureHelper;

public class OnboardingLocationPermissionFragment extends Fragment {

	public static final int REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION = 123;

	private Button locationButton;
	private Button continueButton;

	public static OnboardingLocationPermissionFragment newInstance() {
		return new OnboardingLocationPermissionFragment();
	}

	public OnboardingLocationPermissionFragment() {
		super(R.layout.fragment_onboarding_permission_location);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		locationButton = view.findViewById(R.id.onboarding_location_permission_button);
		locationButton.setOnClickListener(v -> {
			String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
			requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION);
		});
		continueButton = view.findViewById(R.id.onboarding_location_permission_continue_button);
		continueButton.setOnClickListener(v -> {
			((OnboardingActivity) getActivity()).continueToNextPage();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		updateFragmentState();
	}

	private void updateFragmentState() {
		boolean locationPermissionGranted = DeviceFeatureHelper.isLocationPermissionGranted(requireContext());
		if (locationPermissionGranted) {
			PermissionButtonUtil.setButtonOk(locationButton, R.string.android_onboarding_bt_permission_button_allowed);
		} else {
			PermissionButtonUtil.setButtonDefault(locationButton, R.string.android_onboarding_bt_permission_button);
		}
		continueButton.setVisibility(locationPermissionGranted ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION) {
			if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				if (!ActivityCompat
						.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
					new AlertDialog.Builder(requireActivity())
							.setTitle(R.string.android_button_permission_location)
							.setMessage(R.string.android_foreground_service_notification_error_location_permission)
							.setPositiveButton(getString(R.string.android_button_ok),
									(dialogInterface, i) -> {
										DeviceFeatureHelper.openApplicationSettings(requireActivity());
										dialogInterface.dismiss();
									})
							.create()
							.show();
				}
			}
			((OnboardingActivity) getActivity()).continueToNextPage();
		}
	}

}