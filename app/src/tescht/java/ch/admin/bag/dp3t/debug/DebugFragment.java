/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.debug;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.nearby.ExposureWindowMatchingWorker;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.CertificatePinning;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class DebugFragment extends Fragment {

	public static final boolean EXISTS = true;

	private TracingViewModel tracingViewModel;

	public static void startDebugFragment(FragmentManager parentFragmentManager) {
		parentFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, DebugFragment.newInstance())
				.addToBackStack(DebugFragment.class.getCanonicalName())
				.commit();
	}

	public static DebugFragment newInstance() {
		return new DebugFragment();
	}

	public DebugFragment() {
		super(R.layout.fragment_debug);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		TextView statusText = view.findViewById(R.id.debug_sdk_state_text);
		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			statusText.setText(DebugUtils.formatStatusString(status, view.getContext()));
			boolean isTracing = (status.isTracingEnabled()) && status.getErrors().size() == 0;
			statusText.setBackgroundTintList(ColorStateList.valueOf(
					isTracing ? getResources().getColor(R.color.status_green_bg, null)
							  : getResources().getColor(R.color.status_purple_bg, null)));
		});

		view.findViewById(R.id.debug_button_reset).setOnClickListener(v -> {
			tracingViewModel.resetSdk();
			requireActivity().recreate();
		});

		view.findViewById(R.id.debug_button_reset_onboarding).setOnClickListener(v -> {
			SecureStorage.getInstance(requireContext()).setOnboardingCompleted(false);
			getActivity().finish();
		});

		view.findViewById(R.id.debug_button_reset_update_boarding).setOnClickListener(v -> {
			SecureStorage.getInstance(requireContext()).setLastShownUpdateBoardingVersion(0);
			getActivity().finish();
		});

		view.findViewById(R.id.debug_card_overridestate).setVisibility(View.GONE);

		CheckBox certPinningCheckbox = view.findViewById(R.id.debug_certificate_pinning);
		certPinningCheckbox.setChecked(CertificatePinning.isEnabled());
		certPinningCheckbox.setOnCheckedChangeListener((v, isChecked) -> {
			CertificatePinning.setEnabled(isChecked, v.getContext());
			DP3T.setCertificatePinner(CertificatePinning.getCertificatePinner());
		});

		view.findViewById(R.id.debug_trigger_exposure_check)
				.setOnClickListener(v -> ExposureWindowMatchingWorker.startMatchingWorker(v.getContext()));
	}

}
