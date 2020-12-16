/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.contacts;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.sdk.internal.history.HistoryEntry;
import org.dpppt.android.sdk.internal.history.HistoryEntryType;
import org.dpppt.android.sdk.internal.logger.Logger;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.home.TracingBoxFragment;
import ch.admin.bag.dp3t.home.views.HeaderView;
import ch.admin.bag.dp3t.util.DateUtils;
import ch.admin.bag.dp3t.util.ENExceptionHelper;
import ch.admin.bag.dp3t.util.UrlUtil;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class ContactsFragment extends Fragment {

	private static final String TAG = "ContactsFragment";

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private Switch tracingSwitch;
	private boolean userInitiatedCheckedChange = true;

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
				.add(R.id.status_container, TracingBoxFragment.newInstance(false))
				.commit();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.contacts_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		tracingSwitch = view.findViewById(R.id.contacts_tracing_switch);

		headerView = view.findViewById(R.id.contacts_header_view);
		scrollView = view.findViewById(R.id.contacts_scroll_view);
		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatus -> {
			headerView.setState(tracingStatus);
		});
		setupScrollBehavior();
		setupTracingView();
		setupHistoryCard(view);

		view.findViewById(R.id.contacts_faq_button)
				.setOnClickListener(v -> UrlUtil.openUrl(v.getContext(), getString(R.string.faq_button_url)));
	}

	private void setupTracingView() {
		Activity activity = requireActivity();

		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			setTracingSwitchChecked(status.isTracingEnabled());
		});

		tracingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
			if (isChecked) {
				tracingViewModel.enableTracing(activity,
						() -> { },
						(e) -> {
							String message = ENExceptionHelper.getErrorMessage(e, activity);
							Logger.e(TAG, message);
							new AlertDialog.Builder(activity, R.style.NextStep_AlertDialogStyle)
									.setTitle(R.string.android_en_start_failure)
									.setMessage(message)
									.setPositiveButton(R.string.android_button_ok, (dialog, which) -> {})
									.show();
							setTracingSwitchChecked(false);
						},
						() -> setTracingSwitchChecked(false));
			} else {
				if (userInitiatedCheckedChange) showReactivateTracingReminderDialog();
			}
		});
	}

	private void setTracingSwitchChecked(boolean checked) {
		userInitiatedCheckedChange = false;
		tracingSwitch.setChecked(checked);
		userInitiatedCheckedChange = true;
	}

	private void setupHistoryCard(View view) {
		View historyCard = view.findViewById(R.id.contacts_card_history);
		if (BuildConfig.IS_FLAVOR_PROD || BuildConfig.IS_FLAVOR_ABNAHME) {
			historyCard.findViewById(R.id.card_history_chevron).setVisibility(View.GONE);
		} else {
			historyCard.setOnClickListener(v -> {
				getParentFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.main_fragment_container, HistoryFragment.newInstance())
						.addToBackStack(HistoryFragment.class.getCanonicalName())
						.commit();
			});
		}
		View historyCardLoadingView = view.findViewById(R.id.card_history_loading_view);
		historyCardLoadingView.setVisibility(View.VISIBLE);
		TextView lastSyncDate = view.findViewById(R.id.card_history_last_synchronization_date);

		tracingViewModel.getHistoryLiveDate().observe(getViewLifecycleOwner(), historyEntries -> {
			if (historyEntries != null) {
				Long timeSync = null;
				for (HistoryEntry entry : historyEntries) {
					if (entry.getType() == HistoryEntryType.SYNC && entry.isSuccessful()) {
						timeSync = entry.getTime();
						lastSyncDate.setText(DateUtils.getFormattedDateTime(timeSync));
						break;
					}
				}
				if (timeSync == null) lastSyncDate.setText("-");
				historyCardLoadingView.animate()
						.alpha(0f)
						.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
						.withEndAction(() -> historyCardLoadingView.setVisibility(View.GONE))
						.start();
			}
		});
		tracingViewModel.loadHistoryEntries();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		headerView.stopAnimation();
	}

	private void showReactivateTracingReminderDialog() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.add(ReactivateTracingReminderDialog.newInstance(), ReactivateTracingReminderDialog.class.getCanonicalName())
				.commit();
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
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

}
