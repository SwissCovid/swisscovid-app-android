/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.reports;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.dpppt.android.app.R;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.app.util.DateUtils;
import org.dpppt.android.app.util.NotificationUtil;
import org.dpppt.android.app.util.PhoneUtil;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.sdk.internal.database.models.ExposureDay;

public class ReportsFragment extends Fragment {

	public static ReportsFragment newInstance() {
		return new ReportsFragment();
	}

	private TracingViewModel tracingViewModel;
	private SecureStorage secureStorage;

	private ReportsSlidePageAdapter pagerAdapter;

	private ViewPager2 headerViewPager;
	private LockableScrollView scrollView;
	private View scrollViewFirstchild;
	private CirclePageIndicator circlePageIndicator;

	private View healthyView;
	private View saveOthersView;
	private View hotlineView;
	private View infectedView;

	private Button callHotlineButton1;
	private Button callHotlineButton2;
	private TextView callHotlineLastText1;
	private TextView callHotlineLastText2;

	private boolean hotlineJustCalled = false;

	private int originalHeaderHeight = 0;
	private int originalFirstChildPadding = 0;

	public ReportsFragment() { super(R.layout.fragment_reports); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		secureStorage = SecureStorage.getInstance(getContext());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.reports_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		headerViewPager = view.findViewById(R.id.reports_header_viewpager);
		scrollView = view.findViewById(R.id.reports_scrollview);
		scrollViewFirstchild = view.findViewById(R.id.reports_scrollview_firstChild);
		circlePageIndicator = view.findViewById(R.id.reports_pageindicator);

		healthyView = view.findViewById(R.id.reports_healthy);
		saveOthersView = view.findViewById(R.id.reports_save_others);
		hotlineView = view.findViewById(R.id.reports_hotline);
		infectedView = view.findViewById(R.id.reports_infected);

		callHotlineButton1 = hotlineView.findViewById(R.id.card_encounters_button);
		callHotlineButton2 = saveOthersView.findViewById(R.id.card_encounters_button);
		callHotlineLastText1 = hotlineView.findViewById(R.id.card_encounters_last_call);
		callHotlineLastText2 = saveOthersView.findViewById(R.id.card_encounters_last_call);

		callHotlineButton1.setOnClickListener(view1 -> {
			hotlineJustCalled = true;
			secureStorage.justCalledHotline();
			PhoneUtil.callHelpline(getContext());
		});

		callHotlineButton2.setOnClickListener(view1 -> {
			hotlineJustCalled = true;
			secureStorage.justCalledHotline();
			PhoneUtil.callHelpline(getContext());
		});

		pagerAdapter = new ReportsSlidePageAdapter();
		headerViewPager.setAdapter(pagerAdapter);
		circlePageIndicator.setViewPager(headerViewPager);

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			healthyView.setVisibility(View.GONE);
			saveOthersView.setVisibility(View.GONE);
			hotlineView.setVisibility(View.GONE);
			infectedView.setVisibility(View.GONE);
			List<Pair<ReportsPagerFragment.Type, Long>> items = new ArrayList<>();
			if (tracingStatusInterface.isReportedAsInfected()) {
				infectedView.setVisibility(View.VISIBLE);
				items.add(new Pair<>(ReportsPagerFragment.Type.POSITIVE_TESTED, secureStorage.getInfectedDate()));
			} else if (tracingStatusInterface.wasContactReportedAsExposed()) {
				List<ExposureDay> exposureDays = tracingStatusInterface.getExposureDays();
				boolean isHotlineCallPending = secureStorage.isHotlineCallPending();
				if (isHotlineCallPending) {
					hotlineView.setVisibility(View.VISIBLE);
				} else {
					saveOthersView.setVisibility(View.VISIBLE);
				}
				for (int i = 0; i < exposureDays.size(); i++) {
					ExposureDay exposureDay = exposureDays.get(i);
					if (i == 0) {
						items.add(new Pair<>(ReportsPagerFragment.Type.POSSIBLE_INFECTION,
								exposureDay.getExposedDate().getStartOfDay(TimeZone.getDefault())));
					} else {
						items.add(new Pair<>(ReportsPagerFragment.Type.NEW_CONTACT,
								exposureDay.getExposedDate().getStartOfDay(TimeZone.getDefault())));
					}
				}
			} else {
				healthyView.setVisibility(View.VISIBLE);
				items.add(new Pair<>(ReportsPagerFragment.Type.NO_REPORTS, null));
			}
			pagerAdapter.updateItems(items);
		});

		NotificationManager notificationManager =
				(NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NotificationUtil.NOTIFICATION_ID_CONTACT);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (hotlineJustCalled) {
			hotlineJustCalled = false;
			hotlineView.setVisibility(View.GONE);
			saveOthersView.setVisibility(View.VISIBLE);
		}

		long lastHotlineCallTimestamp = secureStorage.lastHotlineCallTimestamp();
		if (lastHotlineCallTimestamp != 0) {
			((TextView) hotlineView.findViewById(R.id.card_encounters_title)).setText(R.string.meldungen_detail_call_again);

			String date = DateUtils.getFormattedTimestamp(lastHotlineCallTimestamp);
			date = getString(R.string.meldungen_detail_call_last_call).replace("{DATE}", date);
			callHotlineLastText1.setText(date);
			callHotlineLastText2.setText(date);
		} else {
			callHotlineLastText1.setText("");
			callHotlineLastText2.setText("");
		}
	}

	public void doHeaderAnimation(View info, View image, Button button) {
		secureStorage.setReportsHeaderAnimationPending(false);

		ViewGroup rootView = (ViewGroup) getView();

		scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
				rootView.getHeight(),
				scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
		scrollViewFirstchild.setVisibility(View.VISIBLE);

		rootView.post(() -> {

			AutoTransition autoTransition = new AutoTransition();
			autoTransition.setDuration(300);
			autoTransition.addListener(new Transition.TransitionListener() {
				@Override
				public void onTransitionStart(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionEnd(@NonNull Transition transition) {
					headerViewPager.post(() -> {
						setupScrollBehavior();
					});
				}

				@Override
				public void onTransitionCancel(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionPause(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionResume(@NonNull Transition transition) {

				}
			});

			TransitionManager.beginDelayedTransition(rootView, autoTransition);

			ViewGroup.LayoutParams headerLp = headerViewPager.getLayoutParams();
			headerLp.height = originalHeaderHeight;
			headerViewPager.setLayoutParams(headerLp);

			scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
					originalFirstChildPadding,
					scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());

			info.setVisibility(View.VISIBLE);
			image.setVisibility(View.GONE);
			button.setVisibility(View.GONE);

			circlePageIndicator.setVisibility(View.VISIBLE);
			headerViewPager.setUserInputEnabled(true);
		});
	}

	private void setupScrollBehavior() {
		if (!isVisible()) return;

		Rect rect = new Rect();
		headerViewPager.getDrawingRect(rect);
		scrollView.setScrollPreventRect(rect);

		int scrollRangePx = scrollViewFirstchild.getPaddingTop();
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);
		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = computeScrollAnimProgress(scrollY, scrollRangePx);
			headerViewPager.setAlpha(1 - progress);
			headerViewPager.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerViewPager.setAlpha(1 - progress);
			headerViewPager.setTranslationY(progress * translationRangePx);
		});
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

	private class ReportsSlidePageAdapter extends FragmentStateAdapter {

		List<Pair<ReportsPagerFragment.Type, Long>> items = new ArrayList<>();

		boolean isReportsHeaderAnimationPending = false;

		ReportsSlidePageAdapter() {
			super(ReportsFragment.this);
		}

		void updateItems(List<Pair<ReportsPagerFragment.Type, Long>> items) {

			isReportsHeaderAnimationPending = secureStorage.isReportsHeaderAnimationPending();

			this.items.clear();
			this.items.addAll(items);
			notifyDataSetChanged();

			if (items.size() > 1) {
				if (!isReportsHeaderAnimationPending) circlePageIndicator.setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams lp = headerViewPager.getLayoutParams();
				lp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports_with_indicator);
				headerViewPager.setLayoutParams(lp);
				scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
						getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports_width_indicator),
						scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
			} else {
				circlePageIndicator.setVisibility(View.GONE);
				ViewGroup.LayoutParams lp = headerViewPager.getLayoutParams();
				lp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports);
				headerViewPager.setLayoutParams(lp);
				scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
						getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports),
						scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
			}

			if (isReportsHeaderAnimationPending) {
				headerViewPager.setUserInputEnabled(false);

				ViewGroup.LayoutParams headerLp = headerViewPager.getLayoutParams();
				originalHeaderHeight = headerLp.height;
				headerLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
				headerViewPager.setLayoutParams(headerLp);

				originalFirstChildPadding = scrollViewFirstchild.getPaddingTop();

				scrollViewFirstchild.setVisibility(View.GONE);
			}

			headerViewPager.post(() -> {
				headerViewPager.setCurrentItem(items.size() - 1, false);

				setupScrollBehavior();
			});
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {

			Pair<ReportsPagerFragment.Type, Long> item = items.get(position);
			ReportsPagerFragment.Type type = item.first;
			long timestamp = item.second == null ? 0 : item.second;

			boolean showAnimationControls = isReportsHeaderAnimationPending && position == items.size() - 1;

			switch (type) {
				case NO_REPORTS:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.NO_REPORTS, 0, false);
				case POSSIBLE_INFECTION:
					return ReportsPagerFragment
							.newInstance(ReportsPagerFragment.Type.POSSIBLE_INFECTION, timestamp, showAnimationControls);
				case NEW_CONTACT:
					return ReportsPagerFragment
							.newInstance(ReportsPagerFragment.Type.NEW_CONTACT, timestamp, showAnimationControls);
				case POSITIVE_TESTED:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.POSITIVE_TESTED, timestamp, false);
			}

			throw new IllegalArgumentException();
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

	}

}
