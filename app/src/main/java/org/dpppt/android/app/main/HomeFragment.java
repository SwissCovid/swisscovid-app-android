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
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.app.contacts.ContactsFragment;
import org.dpppt.android.app.debug.DebugFragment;
import org.dpppt.android.app.util.DebugUtils;
import org.dpppt.android.app.main.views.HeaderView;
import org.dpppt.android.app.notifications.NotificationsFragment;
import org.dpppt.android.app.trigger.TriggerFragment;
import org.dpppt.android.app.util.TracingStatusHelper;
import org.dpppt.android.app.whattodo.WtdPositiveTestFragment;
import org.dpppt.android.app.whattodo.WtdSymptomsFragment;
import org.dpppt.android.sdk.TracingStatus;

public class HomeFragment extends Fragment {

	private static final String STATE_SCROLL_VIEW = "STATE_SCROLL_VIEW";

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

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
		setupHeader(view);
		setupStatusElements(view);
		setupWhatToDo(view);
		setupDebugButton(view);

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

	private void setupHeader(View view) {
		headerView = view.findViewById(R.id.home_header_container);
		tracingViewModel.getAppStateLiveData()
				.observe(getViewLifecycleOwner(), appState -> headerView.setState(appState));
	}

	private void setupStatusElements(View view) {
		view.findViewById(R.id.card_contacts).setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, ContactsFragment.newInstance())
						.addToBackStack(ContactsFragment.class.getCanonicalName())
						.commit());
		View contactStatusView = view.findViewById(R.id.contacts_status);
		tracingViewModel.getTracingEnabledLiveData().observe(getViewLifecycleOwner(),
				isTracing -> {
					List<TracingStatus.ErrorState> errors = tracingViewModel.getErrorsLiveData().getValue();
					TracingStatusHelper.State state = errors.size() > 0 || !isTracing ? TracingStatusHelper.State.WARNING :
													  TracingStatusHelper.State.OK;
					int titleRes = state == TracingStatusHelper.State.OK ? R.string.tracing_active_title
																		 : R.string.tracing_error_title;
					int textRes = state == TracingStatusHelper.State.OK ? R.string.tracing_active_text
																		: R.string.tracing_error_text;
					TracingStatusHelper.updateStatusView(contactStatusView, state, titleRes, textRes);
				});

		view.findViewById(R.id.card_notifications).setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, NotificationsFragment.newInstance())
						.addToBackStack(NotificationsFragment.class.getCanonicalName())
						.commit());
		View notificationStatusBubble = view.findViewById(R.id.notifications_status_bubble);
		View notificationStatusView = notificationStatusBubble.findViewById(R.id.notification_status);

		View buttonInform = view.findViewById(R.id.main_button_inform);
		buttonInform.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, TriggerFragment.newInstance())
						.addToBackStack(TriggerFragment.class.getCanonicalName())
						.commit());

		tracingViewModel.getSelfOrContactExposedLiveData().observe(getViewLifecycleOwner(),
				selfOrContactExposed -> {
					boolean isExposed = selfOrContactExposed.first || selfOrContactExposed.second;
					buttonInform.setVisibility(!selfOrContactExposed.first ? View.VISIBLE : View.GONE);
					TracingStatusHelper.State state =
							!(isExposed) ? TracingStatusHelper.State.OK
										 : TracingStatusHelper.State.INFO;
					int title = isExposed ? (selfOrContactExposed.first ? R.string.meldungen_infected_title
																		: R.string.meldungen_meldung_title)
										  : R.string.meldungen_no_meldungen_title;
					int text = isExposed ? (selfOrContactExposed.first ? R.string.meldungen_infected_text :
											R.string.meldungen_meldung_text)
										 : R.string.meldungen_no_meldungen_text;

					notificationStatusBubble.setBackgroundTintList(ColorStateList
							.valueOf(getContext().getColor(isExposed ? R.color.status_blue : R.color.status_green_bg)));
					TracingStatusHelper.updateStatusView(notificationStatusView, state, title, text);
				});
	}

	private void setupWhatToDo(View view) {
		view.findViewById(R.id.card_what_to_do_symptoms).setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, WtdSymptomsFragment.newInstance())
						.addToBackStack(WtdSymptomsFragment.class.getCanonicalName())
						.commit());
		view.findViewById(R.id.card_what_to_do_test).setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, WtdPositiveTestFragment.newInstance())
						.addToBackStack(WtdPositiveTestFragment.class.getCanonicalName())
						.commit());
	}

	private void setupDebugButton(View view) {
		View debugButton = view.findViewById(R.id.main_button_debug);
		if (DebugUtils.isDev()) {
			debugButton.setVisibility(View.VISIBLE);
			debugButton.setOnClickListener(
					v -> DebugFragment.startDebugFragment(getParentFragmentManager()));
		} else {
			debugButton.setVisibility(View.GONE);
		}
	}

}
