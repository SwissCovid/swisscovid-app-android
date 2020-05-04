/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
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

import ch.admin.bag.dp3t.onboarding.util.PermissionButtonUtil;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.DeviceFeatureHelper;

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
			PermissionButtonUtil.setButtonOk(batteryButton, R.string.android_onboarding_battery_permission_button_deactivated);
		} else {
			PermissionButtonUtil.setButtonDefault(batteryButton, R.string.android_onboarding_battery_permission_button);
		}
		continueButton.setVisibility(batteryOptDeactivated || wasUserActive ? View.VISIBLE : View.GONE);

		if (batteryOptDeactivated && wasUserActive) {
			((OnboardingActivity) getActivity()).continueToNextPage();
		}
	}

}