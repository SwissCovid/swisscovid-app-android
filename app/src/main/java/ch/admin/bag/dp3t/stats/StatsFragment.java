/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.stats;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import java.util.ArrayList;
import java.util.List;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.models.HistoryDataPointModel;
import ch.admin.bag.dp3t.networking.models.StatsResponseModel;
import ch.admin.bag.dp3t.util.FormatUtil;
import ch.admin.bag.dp3t.util.UiUtils;
import ch.admin.bag.dp3t.util.UrlUtil;

public class StatsFragment extends Fragment {

	private static final int DIAGRAM_HISTORY_DAY_COUNT = 28;
	private static final long ANIMATION_DURATION = 600L;

	private StatsViewModel statsViewModel;

	Toolbar toolbar;
	private ScrollView scrollView;
	private ImageView headerView;

	private ConstraintLayout statsConstraintLayout;

	private ViewGroup totalActiveUsersCardContent;
	private TextView totalActiveusers;

	private ViewGroup covidcodesCard;
	private ImageButton covidcodesInfoButton;
	private TextView totalCovidcodesEntered;
	private TextView totalCovidcodesEnteredLastTwoDays;

	private ViewGroup casesCard;
	private ImageButton casesInfoButton;
	private TextView casesSevenDayAverage;
	private TextView casesPreviousWeekChange;

	private DiagramView diagramView;
	private HorizontalScrollView diagramScrollView;
	private View scrollViewWidener;
	private DiagramYAxisView diagramYAxisView;

	private View errorView;
	private TextView errorRetryButton;
	private View progressView;

	private View moreStatsButton;

	private Button shareAppButton;

	public static StatsFragment newInstance() {
		return new StatsFragment();
	}

	public StatsFragment() {
		super(R.layout.fragment_stats);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		statsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		toolbar = view.findViewById(R.id.main_toolbar);
		scrollView = view.findViewById(R.id.stats_scroll_view);
		headerView = view.findViewById(R.id.header_view);

		statsConstraintLayout = view.findViewById(R.id.stats_constraint_layout);

		totalActiveUsersCardContent = view.findViewById(R.id.stats_active_users_card_content);
		totalActiveusers = view.findViewById(R.id.stats_total_active_users);

		covidcodesCard = view.findViewById(R.id.stats_covidcodes_card);
		covidcodesInfoButton = view.findViewById(R.id.stats_covidcodes_info_button);
		totalCovidcodesEntered = view.findViewById(R.id.stats_covidcodes_total_value);
		totalCovidcodesEnteredLastTwoDays = view.findViewById(R.id.stats_covidcodes_two_days_value);

		casesCard = view.findViewById(R.id.stats_diagram_card);
		casesInfoButton = view.findViewById(R.id.stats_cases_info_button);
		casesSevenDayAverage = view.findViewById(R.id.stats_cases_seven_day_average_value);
		casesPreviousWeekChange = view.findViewById(R.id.stats_cases_previous_week_change_value);

		diagramView = view.findViewById(R.id.diagram_view);
		diagramScrollView = view.findViewById(R.id.diagram_scroll_view);
		scrollViewWidener = view.findViewById(R.id.scroll_view_widener);
		diagramYAxisView = view.findViewById(R.id.diagram_y_axis_view);

		errorView = view.findViewById(R.id.error_view);
		errorRetryButton = view.findViewById(R.id.button_retry);
		progressView = view.findViewById(R.id.progress_view);

		moreStatsButton = view.findViewById(R.id.stats_more);

		shareAppButton = view.findViewById(R.id.share_app_button);

		setupScrollBehavior();
		setupDiagramScrollBehavior();
		setupMoreStatsButton();
		setupShareAppButton();
		setupInfoButtons();

		statsViewModel.getStatsLiveData().observe(getViewLifecycleOwner(), this::onStatsOutcomeChanged);
	}

	private void setupScrollBehavior() {
		int scrollRangePx = getResources().getDimensionPixelSize(R.dimen.top_item_padding);
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);

		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = UiUtils.computeScrollAnimProgress(scrollY, scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = UiUtils.computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
	}

	private void setupDiagramScrollBehavior() {
		diagramScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			diagramView.setScrollX(scrollX);
		});
	}

	private void setupMoreStatsButton() {
		moreStatsButton.setOnClickListener(v -> {
			String url = v.getContext().getResources().getString(R.string.stats_more_statistics_url);
			UrlUtil.openUrl(v.getContext(), url);
		});
	}

	private void setupShareAppButton() {
		shareAppButton.setOnClickListener(v -> {
			String message = v.getContext().getResources().getString(R.string.share_app_message) + "\n" +
					v.getContext().getResources().getString(R.string.share_app_url);

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, message);
			intent.setType("text/pain");

			Intent shareIntent = Intent.createChooser(intent, null);
			startActivity(shareIntent);
		});
	}

	private void setupInfoButtons() {
		covidcodesInfoButton.setOnClickListener(v -> {
			ArrayList<StatsDetailsSection> sections = new ArrayList<>();
			sections.add(new StatsDetailsSection(getString(R.string.stats_covidcodes_total_label),
					getString(R.string.stats_covidcodes_total_description)));
			sections.add(new StatsDetailsSection(getString(R.string.stats_covidcodes_0to2days_label),
					getString(R.string.stats_covidcodes_0to2days_description)));
			StatsDetailsDialogFragment fragment = StatsDetailsDialogFragment.newInstance(
					R.color.blue_main,
					getString(R.string.stats_info_popup_subtitle_covidcodes),
					getString(R.string.stats_info_popup_title),
					sections
			);

			requireActivity().getSupportFragmentManager()
					.beginTransaction()
					.add(fragment, StatsDetailsDialogFragment.class.getCanonicalName())
					.commit();
		});

		casesInfoButton.setOnClickListener(v -> {
			ArrayList<StatsDetailsSection> sections = new ArrayList<>();
			sections.add(new StatsDetailsSection(getString(R.string.stats_cases_current_label),
					getString(R.string.stats_cases_current_description)));
			sections.add(new StatsDetailsSection(getString(R.string.stats_cases_7day_average_label),
					getString(R.string.stats_cases_7day_average_description)));
			sections.add(new StatsDetailsSection(getString(R.string.stats_cases_rel_prev_week_label),
					getString(R.string.stats_cases_rel_prev_week_description)));
			StatsDetailsDialogFragment fragment = StatsDetailsDialogFragment.newInstance(
					R.color.purple_main,
					getString(R.string.stats_info_popup_subtitle_cases),
					getString(R.string.stats_info_popup_title),
					sections
			);

			requireActivity().getSupportFragmentManager()
					.beginTransaction()
					.add(fragment, StatsDetailsDialogFragment.class.getCanonicalName())
					.commit();
		});
	}

	private void onStatsOutcomeChanged(StatsOutcome outcome) {
		// Begin a simultaneous auto transition for bounds and fade transitions
		AutoTransition transition = new AutoTransition();
		transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
		transition.setDuration(ANIMATION_DURATION);
		TransitionManager.beginDelayedTransition(statsConstraintLayout, transition);

		switch (outcome.getOutcome()) {
			case LOADING:
				totalActiveUsersCardContent.setVisibility(View.GONE);
				covidcodesCard.setVisibility(View.GONE);
				casesCard.setVisibility(View.GONE);
				progressView.setVisibility(View.VISIBLE);
				errorView.setVisibility(View.GONE);
				break;
			case ERROR:
				totalActiveUsersCardContent.setVisibility(View.GONE);
				covidcodesCard.setVisibility(View.GONE);
				casesCard.setVisibility(View.GONE);
				progressView.setVisibility(View.GONE);
				errorView.setVisibility(View.VISIBLE);

				errorRetryButton.setPaintFlags(errorRetryButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
				errorRetryButton.setOnClickListener(v -> statsViewModel.loadStats());
				break;
			case RESULT:
				totalActiveUsersCardContent.setVisibility(View.VISIBLE);
				covidcodesCard.setVisibility(View.VISIBLE);
				casesCard.setVisibility(View.VISIBLE);
				progressView.setVisibility(View.GONE);
				errorView.setVisibility(View.GONE);

				displayStats(outcome.getStatsResponseModel());
				break;
		}
	}

	private void displayStats(StatsResponseModel stats) {
		if (stats.getTotalActiveUsers() != null) {
			countUpActiveUsers(stats.getTotalActiveUsers());
		} else {
			totalActiveusers.setText(FormatUtil.formatNumberInMillions(null));
		}

		totalCovidcodesEntered.setText(FormatUtil.formatNumberInThousands(stats.getTotalCovidcodesEntered()));
		totalCovidcodesEnteredLastTwoDays.setText(FormatUtil.formatPercentage(stats.getCovidcodesEntered0to2dPrevWeek(), 0, false));

		casesSevenDayAverage.setText(FormatUtil.formatNumberInThousands(stats.getNewInfectionsSevenDayAvg()));
		casesPreviousWeekChange.setText(FormatUtil.formatPercentage(stats.getNewInfectionsSevenDayAvgRelPrevWeek(), 0, true));

		List<HistoryDataPointModel> fullHistory = stats.getHistory();
		List<HistoryDataPointModel> diagramHistory;
		if (fullHistory.size() >= DIAGRAM_HISTORY_DAY_COUNT) {
			diagramHistory = fullHistory.subList(fullHistory.size() - DIAGRAM_HISTORY_DAY_COUNT, fullHistory.size());
		} else {
			diagramHistory = fullHistory;
		}

		diagramView.setHistory(diagramHistory);
		diagramYAxisView.setMaxYValue(DiagramView.findMaxYValue(diagramHistory));

		int requiredWidth = diagramView.getTotalTheoreticWidth();
		// Setting the width via LayoutParams does NOT work for the direct child of a ScrollView!
		scrollViewWidener.setMinimumWidth(requiredWidth);

		diagramScrollView.post(() -> {
			diagramScrollView.scrollTo(requiredWidth, 0);
			//make sure scroll position of diagramView gets also updated, also if scrollposition of diagramScrollView is
			// already at requiredWidth
			diagramView.setScrollX(diagramScrollView.getScrollX());
		});
	}

	private void countUpActiveUsers(Integer totalActiveUsers) {
		ValueAnimator animator = ValueAnimator.ofInt(0, totalActiveUsers);
		animator.addUpdateListener(animation -> {
			int animationValue = (int) animation.getAnimatedValue();

			String text = totalActiveusers.getContext().getResources().getString(R.string.stats_counter);
			text = text.replace("{COUNT}", FormatUtil.formatNumberInMillions(animationValue));
			totalActiveusers.setText(text);
		});
		animator.setDuration(ANIMATION_DURATION);
		animator.start();
	}

}
