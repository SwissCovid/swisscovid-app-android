/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import org.dpppt.android.app.R;
import org.dpppt.android.app.contacts.ContactsFragment;
import org.dpppt.android.app.debug.DebugFragment;
import org.dpppt.android.app.main.views.HeaderView;
import org.dpppt.android.app.reports.ReportsFragment;
import org.dpppt.android.app.util.DebugUtils;
import org.dpppt.android.app.util.TracingStatusHelper;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.app.whattodo.WtdPositiveTestFragment;
import org.dpppt.android.app.whattodo.WtdSymptomsFragment;
import org.dpppt.android.sdk.TracingStatus;

public class HomeFragment extends Fragment {

	private static final String STATE_SCROLL_VIEW = "STATE_SCROLL_VIEW";

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private View tracingCard;
	private View tracingStatusView;
	private View cardNotifications;
	private View reportStatusBubble;
	private View reportStatusView;
	private View cardSymptoms;
	private View cardTest;

	public HomeFragment() {
		super(R.layout.fragment_home);
	}

	public static HomeFragment newInstance() {
		return new HomeFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		tracingCard = view.findViewById(R.id.card_tracing);
		tracingStatusView = view.findViewById(R.id.tracing_status);
		cardNotifications = view.findViewById(R.id.card_notifications);
		reportStatusBubble = view.findViewById(R.id.report_status_bubble);
		reportStatusView = reportStatusBubble.findViewById(R.id.report_status);
		headerView = view.findViewById(R.id.home_header_container);

		cardSymptoms = view.findViewById(R.id.card_what_to_do_symptoms);
		cardTest = view.findViewById(R.id.card_what_to_do_test);

		setupHeader();
		setupTracingError();
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

	private void setupTracingError() {
		tracingCard.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, ContactsFragment.newInstance())
						.addToBackStack(ContactsFragment.class.getCanonicalName())
						.commit());
		tracingViewModel.getTracingEnabledLiveData().observe(getViewLifecycleOwner(),
				isTracing -> {
					List<TracingStatus.ErrorState> errors = tracingViewModel.getErrorsLiveData().getValue();
					TracingStatusHelper.State state = errors.size() > 0 || !isTracing ? TracingStatusHelper.State.WARNING :
													  TracingStatusHelper.State.OK;
					int titleRes = state == TracingStatusHelper.State.OK ? R.string.tracing_active_title
																		 : R.string.tracing_error_title;
					int textRes = state == TracingStatusHelper.State.OK ? R.string.tracing_active_text
																		: R.string.tracing_error_text;
					TracingStatusHelper.updateStatusView(tracingStatusView, state, titleRes, textRes);
				});

		cardNotifications.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
						.addToBackStack(ReportsFragment.class.getCanonicalName())
						.commit());

		tracingViewModel.getSelfOrContactExposedLiveData().observe(getViewLifecycleOwner(),
				selfOrContactExposed -> {
					boolean isExposed = selfOrContactExposed.first || selfOrContactExposed.second;
					TracingStatusHelper.State state =
							!(isExposed) ? TracingStatusHelper.State.OK
										 : TracingStatusHelper.State.INFO;
					int title = isExposed ? (selfOrContactExposed.first ? R.string.meldungen_infected_title
																		: R.string.meldungen_meldung_title)
										  : R.string.meldungen_no_meldungen_title;
					int text = isExposed ? (selfOrContactExposed.first ? R.string.meldungen_infected_text :
											R.string.meldungen_meldung_text)
										 : R.string.meldungen_no_meldungen_text;

					reportStatusBubble.setBackgroundTintList(ColorStateList
							.valueOf(getContext().getColor(isExposed ? R.color.status_blue : R.color.status_green_bg)));
					TracingStatusHelper.updateStatusView(reportStatusView, state, title, text);
				});
	}

	private void setupNotification() {

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

}
