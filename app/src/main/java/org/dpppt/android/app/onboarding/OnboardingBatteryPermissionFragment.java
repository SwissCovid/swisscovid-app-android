/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.R;
import org.dpppt.android.app.util.DeviceFeatureHelper;

import static org.dpppt.android.app.onboarding.util.PermissionButtonUtil.setButtonDefault;
import static org.dpppt.android.app.onboarding.util.PermissionButtonUtil.setButtonOk;

public class OnboardingBatteryPermissionFragment extends Fragment {

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
			startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
					Uri.parse("package:" + requireContext().getPackageName())));
			wasUserActive = true;
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
			setButtonOk(batteryButton, R.string.onboarding_android_battery_permission_button_deactivated);
		} else {
			setButtonDefault(batteryButton, R.string.onboarding_android_battery_permission_button);
		}
		continueButton.setVisibility(batteryOptDeactivated ? View.VISIBLE : View.GONE);

		if (batteryOptDeactivated && wasUserActive) {
			wasUserActive = false;
			((OnboardingActivity) getActivity()).continueToNextPage();
		}
	}

}