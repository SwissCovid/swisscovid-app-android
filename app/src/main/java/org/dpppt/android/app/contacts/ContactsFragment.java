/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.contacts;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collection;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.model.TracingState;
import org.dpppt.android.app.main.views.HeaderView;
import org.dpppt.android.app.util.DeviceFeatureHelper;
import org.dpppt.android.app.util.TracingErrorStateHelper;
import org.dpppt.android.app.util.TracingStatusHelper;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.sdk.TracingStatus;

import static org.dpppt.android.app.onboarding.OnboardingLocationPermissionFragment.REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION;

public class ContactsFragment extends Fragment {

	private static final int REQUEST_CODE_BLE_INTENT = 330;

	private TracingViewModel tracingViewModel;

	public static ContactsFragment newInstance() {
		return new ContactsFragment();
	}

	public ContactsFragment() { super(R.layout.fragment_contacts); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		HeaderView headerView = view.findViewById(R.id.contacts_header_view);
		tracingViewModel.getAppStateLiveData()
				.observe(getViewLifecycleOwner(), appState -> {
					headerView.setState(appState);
				});

		View tracingStatusView = view.findViewById(R.id.tracing_status);
		View tracingErrorView = view.findViewById(R.id.tracing_error);

		Switch tracingSwitch = view.findViewById(R.id.contacts_tracing_switch);
		tracingSwitch.setOnClickListener(v -> tracingViewModel.setTracingEnabled(tracingSwitch.isChecked()));

		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			boolean isTracing = status.isAdvertising() && status.isReceiving();
			tracingSwitch.setChecked(isTracing);

			Collection<TracingStatus.ErrorState> errors = tracingViewModel.getErrorsLiveData().getValue();
			if (errors != null && errors.size() > 0) {
				tracingStatusView.setVisibility(View.GONE);
				tracingErrorView.setVisibility(View.VISIBLE);
				TracingStatus.ErrorState errorState = TracingErrorStateHelper.getErrorState(errors);
				TracingErrorStateHelper.updateErrorView(tracingErrorView, errorState);
				tracingErrorView.findViewById(R.id.error_status_button).setOnClickListener(v -> {
					switch (errorState) {
						case MISSING_LOCATION_PERMISSION:
							if (ActivityCompat
									.shouldShowRequestPermissionRationale(requireActivity(),
											Manifest.permission.ACCESS_FINE_LOCATION)) {
								String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
								requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION);
							} else {
								new AlertDialog.Builder(requireActivity())
										.setTitle(R.string.button_permission_location_android)
										.setMessage(R.string.foreground_service_notification_error_location_permission)
										.setPositiveButton(getString(R.string.button_ok),
												(dialogInterface, i) -> {
													DeviceFeatureHelper.openApplicationSettings(requireActivity());
													dialogInterface.dismiss();
												})
										.create()
										.show();
							}
						case BLE_DISABLED:
							BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
							if (!mBluetoothAdapter.isEnabled()) {
								Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
								startActivityForResult(enableBtIntent, REQUEST_CODE_BLE_INTENT);
							}
					}
				});
			} else if (!isTracing) {
				tracingStatusView.setVisibility(View.GONE);
				tracingErrorView.setVisibility(View.VISIBLE);
				TracingStatusHelper.showTracingDeactivated(tracingErrorView);
			} else {
				tracingStatusView.setVisibility(View.VISIBLE);
				tracingErrorView.setVisibility(View.GONE);
				TracingStatusHelper.updateStatusView(tracingStatusView, TracingState.ACTIVE);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		tracingViewModel.invalidateService();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode == REQUEST_CODE_BLE_INTENT) {
			tracingViewModel.invalidateService();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION) {
			if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				tracingViewModel.invalidateService();
			}
		}
	}

}
