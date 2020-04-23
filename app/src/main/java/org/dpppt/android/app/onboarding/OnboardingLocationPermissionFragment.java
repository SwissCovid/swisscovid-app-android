/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.onboarding;

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

import org.dpppt.android.app.R;
import org.dpppt.android.app.util.DeviceFeatureHelper;

import static org.dpppt.android.app.onboarding.util.PermissionButtonUtil.setButtonDefault;
import static org.dpppt.android.app.onboarding.util.PermissionButtonUtil.setButtonOk;

public class OnboardingLocationPermissionFragment extends Fragment {

	private static final int REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION = 123;

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
			setButtonOk(locationButton, R.string.onboarding_android_bt_permission_button_allowed);
		} else {
			setButtonDefault(locationButton, R.string.onboarding_android_bt_permission_button);
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
							.setTitle(R.string.button_permission_location_android)
							.setMessage(R.string.foreground_service_notification_error_location_permission)
							.setPositiveButton(getString(R.string.button_ok),
									(dialogInterface, i) -> {
										DeviceFeatureHelper.openApplicationSettings(requireActivity());
										dialogInterface.dismiss();
									})
							.create()
							.show();
				}
			} else {
				((OnboardingActivity) getActivity()).continueToNextPage();
			}
		}
	}

}