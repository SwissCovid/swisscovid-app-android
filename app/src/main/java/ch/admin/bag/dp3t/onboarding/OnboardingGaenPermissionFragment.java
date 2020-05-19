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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.GaenAvailability;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.onboarding.util.PermissionButtonUtil;
import ch.admin.bag.dp3t.util.DeviceFeatureHelper;
import ch.admin.bag.dp3t.util.InfoDialog;

public class OnboardingGaenPermissionFragment extends Fragment {

	private Button activateButton;
	private Button continueButton;

	private	InfoDialog playServicesUpdateDialog;

	private boolean wasUserActive = false;

	public static OnboardingGaenPermissionFragment newInstance() {
		return new OnboardingGaenPermissionFragment();
	}

	public OnboardingGaenPermissionFragment() {
		super(R.layout.fragment_onboarding_permission_gaen);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		activateButton = view.findViewById(R.id.onboarding_gaen_button);
		activateButton.setOnClickListener(v -> {
			checkGaen();
		});
		continueButton = view.findViewById(R.id.onboarding_gaen_continue_button);
		continueButton.setOnClickListener(v -> {
			((OnboardingActivity) requireActivity()).continueToNextPage();
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (playServicesUpdateDialog != null) {
			playServicesUpdateDialog.dismissAllowingStateLoss();
			playServicesUpdateDialog = null;
			checkGaen();
		}
	}

	private void checkGaen() {
		DP3T.checkGaenAvailability(requireContext(), availability -> {
			switch (availability) {
				case AVAILABLE:
					activateGaen();
					break;
				case UPDATE_REQUIRED:
				case UNAVAILABLE:
					showPlayServicesUpdate(availability);
					break;
			}
		});
	}

	private void showPlayServicesUpdate(GaenAvailability availability) {
		playServicesUpdateDialog = InfoDialog.newInstance(
				R.string.playservices_title,
				R.string.playservices_text,
				availability == GaenAvailability.UPDATE_REQUIRED ? R.string.playservices_update : R.string.playservices_install
		);
		playServicesUpdateDialog.setButtonOnClickListener(v -> {
			DeviceFeatureHelper.openPlayServicesInPlayStore(v.getContext());
		});
		playServicesUpdateDialog.show(getChildFragmentManager(), InfoDialog.class.getCanonicalName());
	}

	private void activateGaen() {
		wasUserActive = true;
		DP3T.start(requireActivity(),
				() -> {
					updateFragmentState(true);
					((OnboardingActivity) requireActivity()).continueToNextPage();
				},
				(e) -> {
					InfoDialog.newInstance(e.getLocalizedMessage())
							.show(getChildFragmentManager(), InfoDialog.class.getCanonicalName());
					updateFragmentState(false);
				},
				() -> {
					updateFragmentState(false);
				});
	}

	private void updateFragmentState(boolean activated) {
		if (activated) {
			PermissionButtonUtil.setButtonOk(activateButton, R.string.onboarding_gaen_button_activated);
		} else {
			PermissionButtonUtil.setButtonDefault(activateButton, R.string.onboarding_gaen_button_activate);
		}
		continueButton.setVisibility(activated || wasUserActive ? View.VISIBLE : View.GONE);
	}

}