/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collection;

import org.dpppt.android.app.R;
import org.dpppt.android.app.contacts.ContactsFragment;
import org.dpppt.android.app.debug.DebugFragment;
import org.dpppt.android.app.main.model.NotificationState;
import org.dpppt.android.app.main.model.TracingState;
import org.dpppt.android.app.main.views.HeaderView;
import org.dpppt.android.app.reports.ReportsFragment;
import org.dpppt.android.app.util.DebugUtils;
import org.dpppt.android.app.util.DeviceFeatureHelper;
import org.dpppt.android.app.util.NotificationStateHelper;
import org.dpppt.android.app.util.TracingErrorStateHelper;
import org.dpppt.android.app.util.TracingStatusHelper;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.app.whattodo.WtdPositiveTestFragment;
import org.dpppt.android.app.whattodo.WtdSymptomsFragment;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;

import static org.dpppt.android.app.onboarding.OnboardingLocationPermissionFragment.REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION;

public class HomeFragment extends Fragment {

	private static final String ARG_KEY_LAUNCH_REPORTS_DIRECTLY = "ARG_KEY_LAUNCH_REPORTS_DIRECTLY";

	private static final String STATE_SCROLL_VIEW = "STATE_SCROLL_VIEW";
	private static final int REQUEST_CODE_BLE_INTENT = 330;

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private View tracingCard;
	private View tracingStatusView;
	private View cardNotifications;
	private View reportStatusBubble;
	private View reportStatusView;
	private View reportErrorView;
	private View cardSymptoms;
	private View cardSymptomsFrame;
	private View cardTest;
	private View cardTestFrame;
	private View tracingErrorView;

	public HomeFragment() {
		super(R.layout.fragment_home);
	}

	public static HomeFragment newInstance(boolean launchReportsFragment) {
		Bundle args = new Bundle();
		args.putBoolean(ARG_KEY_LAUNCH_REPORTS_DIRECTLY, launchReportsFragment);
		HomeFragment fragment = new HomeFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);

		if (savedInstanceState == null && getArguments().getBoolean(ARG_KEY_LAUNCH_REPORTS_DIRECTLY, false)) {
			getParentFragmentManager().beginTransaction()
					.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
					.addToBackStack(ReportsFragment.class.getCanonicalName())
					.commit();
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		tracingCard = view.findViewById(R.id.card_tracing);
		tracingStatusView = view.findViewById(R.id.tracing_status);
		tracingErrorView = view.findViewById(R.id.tracing_error);
		cardNotifications = view.findViewById(R.id.card_notifications);
		reportStatusBubble = view.findViewById(R.id.report_status_bubble);
		reportStatusView = reportStatusBubble.findViewById(R.id.report_status);
		reportErrorView = reportStatusBubble.findViewById(R.id.report_errors);
		headerView = view.findViewById(R.id.home_header_container);

		cardSymptoms = view.findViewById(R.id.card_what_to_do_symptoms);
		cardSymptomsFrame = view.findViewById(R.id.frame_card_symptoms);
		cardTest = view.findViewById(R.id.card_what_to_do_test);
		cardTestFrame = view.findViewById(R.id.frame_card_test);

		setupHeader();
		setupTracingView();
		setupNotification();
		setupWhatToDo();
		setupDebugButton();

		scrollView = view.findViewById(R.id.home_scroll_view);
		if (savedInstanceState != null) {
			scrollView.setScrollY(savedInstanceState.getInt(STATE_SCROLL_VIEW));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		tracingViewModel.invalidateTracingStatus();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SCROLL_VIEW, scrollView.getScrollY());
	}

	private void setupHeader() {
		tracingViewModel.getAppStateLiveData()
				.observe(getViewLifecycleOwner(), appState -> {
					headerView.setState(appState);
				});
	}

	private void setupTracingView() {

		tracingCard.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, ContactsFragment.newInstance())
						.addToBackStack(ContactsFragment.class.getCanonicalName())
						.commit());

		tracingViewModel.getTracingEnabledLiveData().observe(getViewLifecycleOwner(),
				isTracing -> {
					Collection<TracingStatus.ErrorState> errors = tracingViewModel.getErrorsLiveData().getValue();
					tracingCard.findViewById(R.id.contacs_chevron).setVisibility(View.GONE);
					cardSymptomsFrame.setVisibility(View.VISIBLE);
					cardTestFrame.setVisibility(View.VISIBLE);
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
					} else if (tracingViewModel.getTracingStatusLiveData().getValue().getInfectionStatus() ==
							InfectionStatus.INFECTED) {
						tracingStatusView.setVisibility(View.VISIBLE);
						tracingErrorView.setVisibility(View.GONE);
						TracingStatusHelper.updateStatusView(tracingStatusView, TracingState.ENDED);
						cardSymptomsFrame.setVisibility(View.GONE);
						cardTestFrame.setVisibility(View.GONE);

						tracingCard.setOnClickListener(null);
						tracingCard.findViewById(R.id.contacs_chevron).setVisibility(View.GONE);
					} else {
						tracingStatusView.setVisibility(View.VISIBLE);
						tracingErrorView.setVisibility(View.GONE);
						TracingStatusHelper.updateStatusView(tracingStatusView, TracingState.ACTIVE);
					}
				});
	}

	private void setupNotification() {
		cardNotifications.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
						.addToBackStack(ReportsFragment.class.getCanonicalName())
						.commit());

		tracingViewModel.getSelfOrContactExposedLiveData().observe(getViewLifecycleOwner(),
				selfOrContactExposed -> {
					if (selfOrContactExposed.first) {
						NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.POSITIVE_TESTED);
					} else if (selfOrContactExposed.second) {
						NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.EXPOSED);
					} else {
						NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.NO_REPORTS);
					}
					if (tracingViewModel.getTracingStatusLiveData().getValue() != null) {
						Collection<TracingStatus.ErrorState> errorStates =
								tracingViewModel.getTracingStatusLiveData().getValue().getErrors();
						if (errorStates != null && errorStates.size() > 0) {
							TracingStatus.ErrorState errorState = TracingErrorStateHelper.getErrorStateForReports(errorStates);
							TracingErrorStateHelper.updateErrorView(reportErrorView, errorState);
						}
					}
				});
	}

	private void setupWhatToDo() {

		cardSymptoms.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, WtdSymptomsFragment.newInstance())
						.addToBackStack(WtdSymptomsFragment.class.getCanonicalName())
						.commit());
		cardTest.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, WtdPositiveTestFragment.newInstance())
						.addToBackStack(WtdPositiveTestFragment.class.getCanonicalName())
						.commit());
	}

	private void setupDebugButton() {
		View debugButton = getView().findViewById(R.id.main_button_debug);
		if (DebugUtils.isDev()) {
			debugButton.setVisibility(View.VISIBLE);
			debugButton.setOnClickListener(
					v -> DebugFragment.startDebugFragment(getParentFragmentManager()));
		} else {
			debugButton.setVisibility(View.GONE);
		}
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
