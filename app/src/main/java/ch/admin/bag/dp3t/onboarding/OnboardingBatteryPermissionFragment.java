/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.onboarding.util.PermissionButtonUtil;
import ch.admin.bag.dp3t.util.DeviceFeatureHelper;

public class OnboardingBatteryPermissionFragment extends Fragment {

	private static final int REQUEST_CODE_BATTERY_OPTIMIZATIONS_INTENT = 421;

	private Button batteryButton;
	private Button continueButton;

	private boolean wasUserActive = false;

	public static OnboardingBatteryPermissionFragment newInstance() {
		return new OnboardingBatteryPermissionFragment();
	}

	public OnboardingBatteryPermissionFragment() {
		super(R.layout.fragment_onboarding_permission_battery);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		batteryButton = view.findViewById(R.id.onboarding_battery_permission_button);
		batteryButton.setOnClickListener(v -> {
			wasUserActive = true;
			Intent batteryIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
			batteryIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
			startActivityForResult(batteryIntent, REQUEST_CODE_BATTERY_OPTIMIZATIONS_INTENT);
		});
		continueButton = view.findViewById(R.id.onboarding_battery_permission_continue_button);
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
		boolean batteryOptDeactivated = DeviceFeatureHelper.isBatteryOptimizationDeactivated(requireContext());
		if (batteryOptDeactivated) {
			PermissionButtonUtil.setButtonOk(batteryButton, R.string.android_onboarding_battery_permission_button_deactivated);
		} else {
			PermissionButtonUtil.setButtonDefault(batteryButton, R.string.android_onboarding_battery_permission_button);
		}
		continueButton.setVisibility(batteryOptDeactivated || wasUserActive ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS_INTENT) {
			((OnboardingActivity) getActivity()).continueToNextPage();
		}
	}

}