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
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.TimeZone;

import org.crowdnotifier.android.sdk.model.ExposureEvent;
import org.crowdnotifier.android.sdk.storage.ExposureStorage;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.nearby.ExposureWindowMatchingWorker;
import org.dpppt.android.sdk.internal.storage.ExposureDayStorage;
import org.dpppt.android.sdk.models.DayDate;
import org.dpppt.android.sdk.models.ExposureDay;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel;
import ch.admin.bag.dp3t.checkin.models.SwissCovidAssociatedData;
import ch.admin.bag.dp3t.debug.model.DebugAppState;
import ch.admin.bag.dp3t.networking.CertificatePinning;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.NotificationUtil;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class DebugFragment extends Fragment {

	public static final boolean EXISTS = true;

	private TracingViewModel tracingViewModel;
	private CrowdNotifierViewModel crowdNotifierViewModel;

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
		crowdNotifierViewModel = new ViewModelProvider(requireActivity()).get(CrowdNotifierViewModel.class);
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

		view.findViewById(R.id.debug_button_reset_onboarding).setOnClickListener(v -> {
			SecureStorage.getInstance(requireContext()).setOnboardingCompleted(false);
			getActivity().finish();
		});

		view.findViewById(R.id.debug_button_reset_update_boarding).setOnClickListener(v -> {
			SecureStorage.getInstance(requireContext()).setLastShownUpdateBoardingVersion(0);
			getActivity().finish();
		});

		view.findViewById(R.id.debug_button_sync_checkin_keys).setOnClickListener(v -> crowdNotifierViewModel.refreshTraceKeys());

		view.findViewById(R.id.debug_button_simulate_from_instant_app).setOnClickListener(v -> {
			setDebugAppState(DebugAppState.NONE);
			tracingViewModel.resetSdk();
			SecureStorage.getInstance(requireContext()).setOnlyPartialOnboardingCompleted(true);
			getActivity().finish();
		});

		CheckBox certPinningCheckbox = view.findViewById(R.id.debug_certificate_pinning);
		certPinningCheckbox.setChecked(CertificatePinning.isEnabled());
		certPinningCheckbox.setOnCheckedChangeListener((v, isChecked) -> {
			CertificatePinning.setEnabled(isChecked, v.getContext());
			DP3T.setCertificatePinner(CertificatePinning.getCertificatePinner());
		});

		view.findViewById(R.id.debug_trigger_exposure_check)
				.setOnClickListener(v -> ExposureWindowMatchingWorker.startMatchingWorker(v.getContext()));
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
			showExposureDaysInputDialogs();
		});
	}


	private void showInputDialog(String title, String defaultInput, InputDialogCallback callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(title);
		final EditText input = new EditText(getContext());
		input.setText(defaultInput);
		input.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_CLASS_NUMBER);
		builder.setView(input);
		builder.setPositiveButton(R.string.android_button_ok,
				(a, b) -> callback.onResult(Integer.parseInt(input.getText().toString())));
		builder.show();
	}

	private void showExposureDaysInputDialogs() {
		showInputDialog(getString(R.string.number_of_exposure_days), "2", tracingExposures ->
				showInputDialog("How many Checkin Exposures should be simulated?", "2", checkinExposures ->
						exposeMyself(tracingExposures, checkinExposures)));
	}

	private void exposeMyself(int numberOfDays, int numberOfCheckins) {

		ExposureDayStorage eds = ExposureDayStorage.getInstance(requireContext());
		eds.clear();

		for (int i = 0; i < numberOfDays; i++) {
			DayDate dayOfExposure = new DayDate().subtractDays(i);
			ExposureDay exposureDay = new ExposureDay(i, dayOfExposure, System.currentTimeMillis());
			eds.addExposureDays(requireContext(), Arrays.asList(exposureDay));
		}

		ExposureStorage exposureStorage = ExposureStorage.getInstance(requireContext());
		exposureStorage.clear();
		for (int i = 0; i < numberOfCheckins; i++) {
			long exposureStart = new DayDate().subtractDays(i).getStartOfDay(TimeZone.getDefault());
			long exposureEnd = exposureStart + 1000L * 60 * 60;
			exposureStorage.addEntry(new ExposureEvent(-i, exposureStart, exposureEnd, "debug message",
					SwissCovidAssociatedData.getDefaultInstance().toByteArray()));
		}
		if (numberOfCheckins > 0) {
			SecureStorage secureStorage = SecureStorage.getInstance(requireContext());
			NotificationUtil.generateContactNotification(requireContext());
			secureStorage.setAppOpenAfterNotificationPending(true);
			secureStorage.setReportsHeaderAnimationPending(true);
		}
		getActivity().finish();
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

	private interface InputDialogCallback {
		void onResult(int selectedNumber);

	}

}
