/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.contacts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.TracingBoxFragment;
import org.dpppt.android.app.main.views.HeaderView;
import org.dpppt.android.app.viewmodel.TracingViewModel;

import static org.dpppt.android.app.onboarding.OnboardingLocationPermissionFragment.REQUEST_CODE_ASK_PERMISSION_FINE_LOCATION;

public class ContactsFragment extends Fragment {

	private static final int REQUEST_CODE_BLE_INTENT = 330;

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private View tracingStatusView;
	private View tracingErrorView;
	private Switch tracingSwitch;

	public static ContactsFragment newInstance() {
		return new ContactsFragment();
	}

	public ContactsFragment() { super(R.layout.fragment_contacts); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		getChildFragmentManager()
				.beginTransaction()
				.add(R.id.status_container, TracingBoxFragment.newInstance())
				.commit();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		tracingStatusView = view.findViewById(R.id.tracing_status);
		tracingErrorView = view.findViewById(R.id.tracing_error);
		tracingSwitch = view.findViewById(R.id.contacts_tracing_switch);

		headerView = view.findViewById(R.id.contacts_header_view);
		scrollView = view.findViewById(R.id.contacts_scroll_view);
		tracingViewModel.getAppStatusLiveData()
				.observe(getViewLifecycleOwner(), tracingStatus -> {
					headerView.setState(tracingStatus);
				});
		setupScrollBehavior();
		setupTracingView();
	}

	private void setupTracingView() {

		tracingSwitch.setOnClickListener(v -> tracingViewModel.setTracingEnabled(tracingSwitch.isChecked()));

		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			boolean isTracing = status.isAdvertising() && status.isReceiving();
			tracingSwitch.setChecked(isTracing);
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		tracingViewModel.invalidateService();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		headerView.stopAnimation();
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

	private void setupScrollBehavior() {

		int scrollRangePx = getResources().getDimensionPixelSize(R.dimen.top_item_padding);
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);
		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = computeScrollAnimProgress(scrollY, scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		headerView.setAlpha(0);
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

}
