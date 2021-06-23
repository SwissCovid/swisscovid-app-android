/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.home;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.logger.Logger;

import ch.admin.bag.dp3t.MainActivity;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.home.model.TracingState;
import ch.admin.bag.dp3t.onboarding.OnboardingType;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.DeviceFeatureHelper;
import ch.admin.bag.dp3t.util.ENExceptionHelper;
import ch.admin.bag.dp3t.util.TracingErrorStateHelper;
import ch.admin.bag.dp3t.util.TracingStatusHelper;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class TracingBoxFragment extends Fragment {

	private static final String TAG = "TracingBox";

	private static final int REQUEST_CODE_BLE_INTENT = 330;
	private static final int REQUEST_CODE_LOCATION_INTENT = 510;
	private static final int REQUEST_CODE_BATTERY_OPTIMIZATIONS_INTENT = 420;
	private static String ARG_TRACING = "isHomeFragment";
	private TracingViewModel tracingViewModel;

	private View tracingStatusView;

	private View tracingErrorView;
	private boolean isHomeFragment;

	public TracingBoxFragment() {
		super(R.layout.fragment_tracing_box);
	}

	public static TracingBoxFragment newInstance(boolean isTracingFragment) {
		Bundle args = new Bundle();
		args.putBoolean(ARG_TRACING, isTracingFragment);
		TracingBoxFragment fragment = new TracingBoxFragment();
		fragment.setArguments(args);
		return fragment;
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		isHomeFragment = getArguments().getBoolean(ARG_TRACING);
	}


	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		tracingStatusView = view.findViewById(R.id.tracing_status);
		tracingErrorView = view.findViewById(R.id.tracing_error);

		showStatus();
	}

	private void showStatus() {

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			boolean isTracing = tracingStatusInterface.getTracingState().equals(TracingState.ACTIVE);

			TracingStatus.ErrorState errorState = tracingStatusInterface.getTracingErrorState();
			if (SecureStorage.getInstance(requireContext()).getOnlyPartialOnboardingCompleted()) {
				tracingStatusView.setVisibility(View.GONE);
				tracingErrorView.setVisibility(View.VISIBLE);
				TracingStatusHelper.showFinishPartialOnboarding(tracingErrorView);
				tracingErrorView.findViewById(R.id.error_status_button).setOnClickListener(
						v -> ((MainActivity) requireActivity()).launchOnboarding(OnboardingType.NON_INSTANT_PART, null));
			} else if (isTracing && errorState != null) {
				handleErrorState(errorState);
			} else if (tracingStatusInterface.isReportedAsInfected()) {
				tracingStatusView.setVisibility(View.VISIBLE);
				tracingErrorView.setVisibility(View.GONE);
				TracingStatusHelper.updateStatusView(tracingStatusView, TracingState.ENDED);
			} else if (!isTracing) {
				tracingStatusView.setVisibility(View.GONE);
				tracingErrorView.setVisibility(View.VISIBLE);
				TracingStatusHelper.showTracingDeactivated(tracingErrorView, isHomeFragment);
				TextView buttonView = tracingErrorView.findViewById(R.id.error_status_button);
				buttonView.setOnClickListener(v -> enableTracing());
			} else {
				tracingStatusView.setVisibility(View.VISIBLE);
				tracingErrorView.setVisibility(View.GONE);
				TracingStatusHelper.updateStatusView(tracingStatusView, TracingState.ACTIVE);
			}
		});
	}

	private void handleErrorState(TracingStatus.ErrorState errorState) {
		tracingStatusView.setVisibility(View.GONE);
		tracingErrorView.setVisibility(View.VISIBLE);
		TracingErrorStateHelper.updateErrorView(tracingErrorView, errorState);
		tracingErrorView.findViewById(R.id.error_status_button).setOnClickListener(v -> {
			switch (errorState) {
				case BLE_DISABLED:
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					if (!mBluetoothAdapter.isEnabled()) {
						Intent bleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						startActivityForResult(bleIntent, REQUEST_CODE_BLE_INTENT);
					}
					break;
				case BATTERY_OPTIMIZER_ENABLED:
					Intent batteryIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
					batteryIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
					startActivityForResult(batteryIntent, REQUEST_CODE_BATTERY_OPTIMIZATIONS_INTENT);
					break;
				case LOCATION_SERVICE_DISABLED:
					Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivityForResult(locationIntent, REQUEST_CODE_LOCATION_INTENT);
					break;
				case GAEN_UNEXPECTEDLY_DISABLED:
					enableTracing();
					break;
				case GAEN_NOT_AVAILABLE:
					DeviceFeatureHelper.openPlayServicesInPlayStore(v.getContext());
					break;
			}
		});
	}

	private void enableTracing() {
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}

		tracingViewModel.enableTracing(activity,
				() -> { },
				e -> {
					String message = ENExceptionHelper.getErrorMessage(e, activity);
					Logger.e(TAG, message);
					new AlertDialog.Builder(activity, R.style.NextStep_AlertDialogStyle)
							.setTitle(R.string.android_en_start_failure)
							.setMessage(message)
							.setPositiveButton(R.string.android_button_ok, (dialog, which) -> {})
							.show();
				},
				() -> {
					// cancelled
				});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode == REQUEST_CODE_BLE_INTENT && resultCode == Activity.RESULT_OK) {
			tracingViewModel.invalidateTracingStatus();
		} else if (requestCode == REQUEST_CODE_LOCATION_INTENT && resultCode == Activity.RESULT_OK) {
			tracingViewModel.invalidateTracingStatus();
		} else if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS_INTENT && resultCode == Activity.RESULT_OK) {
			tracingViewModel.invalidateTracingStatus();
		}
		DP3T.onActivityResult(getActivity(), requestCode, resultCode, data);
	}

}
