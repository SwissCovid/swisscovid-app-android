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
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.storage.ExposureDayStorage;
import org.dpppt.android.sdk.models.DayDate;
import org.dpppt.android.sdk.models.ExposureDay;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.debug.model.DebugAppState;
import ch.admin.bag.dp3t.networking.CertificatePinning;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class DebugFragment extends Fragment {

	public static final boolean EXISTS = true;

	private TracingViewModel tracingViewModel;

	public static void startDebugFragment(FragmentManager parentFragmentManager) {
		parentFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.root_fragment_container, DebugFragment.newInstance())
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

		setupSdkViews(view);
		setupStateOptions(view);
	}

	private void setupSdkViews(View view) {
		TextView statusText = view.findViewById(R.id.debug_sdk_state_text);
		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			statusText.setText(DebugUtils.formatStatusString(status, view.getContext()));
			boolean isTracing = (status.isTracingEnabled()) && status.getErrors().size() == 0;
			statusText.setBackgroundTintList(ColorStateList.valueOf(
					isTracing ? getResources().getColor(R.color.status_green_bg, null)
							  : getResources().getColor(R.color.status_purple_bg, null)));
		});

		view.findViewById(R.id.debug_button_reset).setOnClickListener(v -> {
			setDebugAppState(DebugAppState.NONE);
			tracingViewModel.resetSdk();
			updateRadioGroup(getView().findViewById(R.id.debug_state_options_group));

			requireActivity().recreate();
		});

		CheckBox certPinningCheckbox = view.findViewById(R.id.debug_certificate_pinning);
		certPinningCheckbox.setChecked(CertificatePinning.isEnabled());
		certPinningCheckbox.setOnCheckedChangeListener((v, isChecked) -> {
			CertificatePinning.setEnabled(isChecked, v.getContext());
			DP3T.setCertificatePinner(CertificatePinning.getCertificatePinner());
		});
	}

	private void setupStateOptions(View view) {
		RadioGroup optionsGroup = view.findViewById(R.id.debug_state_options_group);
		optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
			switch (checkedId) {
				case R.id.debug_state_option_none:
					setDebugAppState(DebugAppState.NONE);
					break;
				case R.id.debug_state_option_healthy:
					setDebugAppState(DebugAppState.HEALTHY);
					break;
				case R.id.debug_state_option_exposed:
					setDebugAppState(DebugAppState.CONTACT_EXPOSED);
					break;
				case R.id.debug_state_option_infected:
					setDebugAppState(DebugAppState.REPORTED_EXPOSED);
					break;
			}
		});

		updateRadioGroup(optionsGroup);

		view.findViewById(R.id.debug_button_testmeldung).setOnClickListener(v -> {
			exposeMyself();
			getActivity().finish();
		});
	}

	private void exposeMyself() {
		ExposureDayStorage eds = ExposureDayStorage.getInstance(requireContext());
		eds.clear();

		DayDate dayOfExposure = new DayDate();
		ExposureDay exposureDay = new ExposureDay(-1, dayOfExposure, System.currentTimeMillis());
		eds.addExposureDay(requireContext(), exposureDay);
	}

	private void updateRadioGroup(RadioGroup optionsGroup) {
		int preSetId = -1;
		switch (getDebugAppState()) {
			case NONE:
				preSetId = R.id.debug_state_option_none;
				break;
			case HEALTHY:
				preSetId = R.id.debug_state_option_healthy;
				break;
			case CONTACT_EXPOSED:
				preSetId = R.id.debug_state_option_exposed;
				break;
			case REPORTED_EXPOSED:
				preSetId = R.id.debug_state_option_infected;
				break;
		}
		optionsGroup.check(preSetId);
	}

	public DebugAppState getDebugAppState() {
		return ((TracingStatusWrapper) tracingViewModel.getTracingStatusInterface()).getDebugAppState();
	}

	public void setDebugAppState(DebugAppState debugAppState) {
		((TracingStatusWrapper) tracingViewModel.getTracingStatusInterface()).setDebugAppState(getContext(), debugAppState);
	}

}
