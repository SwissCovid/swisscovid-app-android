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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.GaenAvailability;
import org.dpppt.android.sdk.internal.logger.Logger;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.onboarding.util.PermissionButtonUtil;
import ch.admin.bag.dp3t.util.DeviceFeatureHelper;
import ch.admin.bag.dp3t.util.ENExceptionHelper;

public class OnboardingGaenPermissionFragment extends Fragment {

	private static final String TAG = "OnboardingGaen";

	private static final String STATE_USER_ACTIVE = "STATE_USER_ACTIVE";

	private Button activateButton;
	private Button continueButton;

	private AlertDialog playServicesUpdateDialog;

	private boolean wasUserActive = false;
	private boolean startedService = false;

	public static OnboardingGaenPermissionFragment newInstance() {
		return new OnboardingGaenPermissionFragment();
	}

	public OnboardingGaenPermissionFragment() {
		super(R.layout.fragment_onboarding_permission_gaen);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			wasUserActive = savedInstanceState.getBoolean(STATE_USER_ACTIVE, false);
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		activateButton = view.findViewById(R.id.onboarding_gaen_button);
		activateButton.setOnClickListener(v -> {
			checkGaen();
			wasUserActive = true;
		});
		continueButton = view.findViewById(R.id.onboarding_gaen_continue_button);
		continueButton.setOnClickListener(v -> {
			((OnboardingActivity) requireActivity()).continueToNextPage();
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (wasUserActive && !startedService) {
			checkGaen();
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_USER_ACTIVE, wasUserActive);
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
		Context context = getContext();
		if (context == null) {
			return;
		}

		if (playServicesUpdateDialog != null) {
			playServicesUpdateDialog.dismiss();
		}

		playServicesUpdateDialog = new AlertDialog.Builder(context, R.style.NextStep_AlertDialogStyle)
				.setTitle(R.string.playservices_title)
				.setMessage(R.string.playservices_text)
				.setPositiveButton(availability == GaenAvailability.UPDATE_REQUIRED ? R.string.playservices_update
																					: R.string.playservices_install,
						(dialog, which) -> DeviceFeatureHelper.openPlayServicesInPlayStore(context))
				.setCancelable(false)
				.show();
	}

	private void activateGaen() {
		OnboardingActivity activity = (OnboardingActivity) getActivity();
		if (activity == null) {
			return;
		}

		startedService = true;
		DP3T.start(activity,
				() -> {
					updateFragmentState(true);
					activity.continueToNextPage();
				},
				(e) -> {
					String message = ENExceptionHelper.getErrorMessage(e, activity);
					Logger.e(TAG, message);
					new AlertDialog.Builder(activity, R.style.NextStep_AlertDialogStyle)
							.setTitle(R.string.android_en_start_failure)
							.setMessage(message)
							.setPositiveButton(R.string.android_button_ok, (dialog, which) -> {})
							.show();
					updateFragmentState(false);
				},
				() -> {
					updateFragmentState(false);
					activity.continueToNextPage();
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