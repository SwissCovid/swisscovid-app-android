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

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.models.HistoryDataPointModel;
import ch.admin.bag.dp3t.networking.models.StatsResponseModel;
import ch.admin.bag.dp3t.util.ToolbarUtil;
import ch.admin.bag.dp3t.util.UiUtils;
import ch.admin.bag.dp3t.util.UrlUtil;

public class StatsFragment extends Fragment {

	private StatsViewModel statsViewModel;

	Toolbar toolbar;
	private ScrollView scrollView;
	private ImageView headerView;

	private TextView totalActiveusers;
	private TextView totalActiveusersText;

	private DiagramView diagramView;
	private DiagramYAxisView diagramYAxisView;

	private TextView lastUpdated;
	private View errorView;
	private TextView errorRetryButton;
	private View progressView;

	private TextView moreStatsButton;

	private Button shareAppButton;

	public static StatsFragment newInstance() {
		return new StatsFragment();
	}

	private StatsFragment() {
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

		toolbar = view.findViewById(R.id.home_toolbar);
		scrollView = view.findViewById(R.id.stats_scroll_view);
		headerView = view.findViewById(R.id.header_view);

		totalActiveusers = view.findViewById(R.id.stats_total_active_users);
		totalActiveusersText = view.findViewById(R.id.stats_total_active_users_text);

		diagramView = view.findViewById(R.id.diagram_view);
		diagramYAxisView = view.findViewById(R.id.diagram_y_axis_view);

		lastUpdated = view.findViewById(R.id.last_updated);
		errorView = view.findViewById(R.id.error_view);
		errorRetryButton = view.findViewById(R.id.button_retry);
		progressView = view.findViewById(R.id.progress_view);

		moreStatsButton = view.findViewById(R.id.stats_more);

		shareAppButton = view.findViewById(R.id.share_app_button);

		ToolbarUtil.setupToolbar(getContext(), toolbar, getParentFragmentManager());
		setupScrollBehavior();
		setupMoreStatsButton();
		setupShareAppButton();

		statsViewModel.getStatsLiveData().observe(getViewLifecycleOwner(), this::displayStats);
		statsViewModel.loadStats();
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

	private void displayStats(StatsOutcome outcome) {
		switch (outcome.getOutcome()) {
			case LOADING:
				totalActiveusers.setVisibility(View.INVISIBLE);
				totalActiveusersText.setVisibility(View.INVISIBLE);

				diagramView.setVisibility(View.GONE);
				diagramYAxisView.setVisibility(View.GONE);
				lastUpdated.setVisibility(View.INVISIBLE);
				errorView.setVisibility(View.GONE);
				errorRetryButton.setVisibility(View.GONE);
				progressView.setVisibility(View.VISIBLE);
				break;
			case ERROR:
				totalActiveusers.setVisibility(View.INVISIBLE);
				totalActiveusersText.setVisibility(View.INVISIBLE);

				diagramView.setVisibility(View.GONE);
				diagramYAxisView.setVisibility(View.GONE);
				lastUpdated.setVisibility(View.INVISIBLE);
				errorView.setVisibility(View.VISIBLE);
				errorRetryButton.setVisibility(View.VISIBLE);
				progressView.setVisibility(View.GONE);

				errorRetryButton.setPaintFlags(errorRetryButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
				errorRetryButton.setOnClickListener(v -> statsViewModel.loadStats());
				break;
			case RESULT:
				StatsResponseModel stats = outcome.getStatsResponseModel();

				totalActiveusers.setVisibility(View.VISIBLE);
				totalActiveusersText.setVisibility(View.VISIBLE);

				diagramView.setVisibility(View.VISIBLE);
				diagramYAxisView.setVisibility(View.VISIBLE);
				// lastUpdated.visibility is set below
				errorView.setVisibility(View.GONE);
				errorRetryButton.setVisibility(View.GONE);
				progressView.setVisibility(View.GONE);

				String text = totalActiveusers.getContext().getResources().getString(R.string.stats_counter);
				text = text.replace("{COUNT}", stats.getTotalActiveUsersInMillions());
				totalActiveusers.setText(text);

				String lastUpdatedDate = stats.getLastUpdatedFormatted();
				if (lastUpdatedDate == null) {
					lastUpdated.setVisibility(View.INVISIBLE);
				} else {
					lastUpdated.setVisibility(View.VISIBLE);
					String text2 = lastUpdated.getContext().getResources().getString(R.string.stats_source_day);
					text2 = text2.replace("{DAY}", lastUpdatedDate);
					lastUpdated.setText(text2);
				}

				List<HistoryDataPointModel> history = stats.getHistory();
				diagramView.setHistory(history);
				diagramYAxisView.setMaxYValue(DiagramView.findMaxYValue(history));
		}
	}

}
