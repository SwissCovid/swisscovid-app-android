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

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.onboarding.util.PermissionButtonUtil;

public class OnboardingGaenPermissionFragment extends Fragment {

	private Button activateButton;
	private Button continueButton;

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

			DP3T.start(getActivity(), () -> {
				((OnboardingActivity) getActivity()).continueToNextPage();
			}, (e) -> {
				// TODO: show error popup and check if exception is play services upgrade needed
				continueButton.setVisibility(View.VISIBLE);
			});
		});
		continueButton = view.findViewById(R.id.onboarding_gaen_continue_button);
		continueButton.setOnClickListener(v -> {
			((OnboardingActivity) getActivity()).continueToNextPage();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void updateFragmentState(boolean activated) {
		if (activated) {
			PermissionButtonUtil.setButtonOk(activateButton, R.string.onboarding_gaen_button_activated);
		} else {
			PermissionButtonUtil.setButtonDefault(activateButton, R.string.onboarding_gaen_button_activate);
		}
		continueButton.setVisibility(activated || wasUserActive ? View.VISIBLE : View.GONE);

		if (activated && wasUserActive) {
			((OnboardingActivity) getActivity()).continueToNextPage();
		}
	}

}