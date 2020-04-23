/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.reports;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import org.dpppt.android.app.R;
import org.dpppt.android.app.viewmodel.TracingViewModel;

public class ReportsFragment extends Fragment {

	public static ReportsFragment newInstance() {
		return new ReportsFragment();
	}

	private TracingViewModel tracingViewModel;

	private FragmentStateAdapter pagerAdapter;

	private NestedScrollableHost header;
	private ViewPager2 viewPager;
	private LockableScrollView scrollView;
	private View scrollViewFirstchild;
	private CirclePageIndicator circlePageIndicator;

	public ReportsFragment() { super(R.layout.fragment_reports); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pagerAdapter = new ReportsSlidePageAdapter();

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.reports_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		header = view.findViewById(R.id.reports_header);
		viewPager = view.findViewById(R.id.reports_viewpager);
		scrollView = view.findViewById(R.id.reports_scrollview);
		scrollViewFirstchild = view.findViewById(R.id.reports_scrollview_firstChild);
		circlePageIndicator = view.findViewById(R.id.reports_pageindicator);

		if (pagerAdapter.getItemCount() > 1) {
			circlePageIndicator.setVisibility(View.VISIBLE);
			ViewGroup.LayoutParams lp = header.getLayoutParams();
			lp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports_with_indicator);
			header.setLayoutParams(lp);
			scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
					getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports_width_indicator),
					scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
		}

		viewPager.setAdapter(pagerAdapter);
		circlePageIndicator.setViewPager(viewPager);

		viewPager.post(() -> {
			Rect rect = new Rect();
			viewPager.getDrawingRect(rect);
			scrollView.setScrollPreventRect(rect);
		});

		/*
		tracingViewModel.getSelfOrContactExposedLiveData().observe(getViewLifecycleOwner(), selfOrContactExposed -> {
			boolean isExposed = selfOrContactExposed.first || selfOrContactExposed.second;
			TracingStatusHelper.State state =
					!(isExposed) ? TracingStatusHelper.State.OK
								 : TracingStatusHelper.State.INFO;
			int title =
					isExposed ? (selfOrContactExposed.first ? R.string.meldungen_infected_title : R.string.meldungen_meldung_title)
							  : R.string.meldungen_no_meldungen_title;
			int text = isExposed ? (selfOrContactExposed.first ? R.string.meldungen_infected_text :
									R.string.meldungen_meldung_text)
								 : R.string.meldungen_no_meldungen_text;
			ColorStateList bubbleColor =
					ColorStateList.valueOf(getContext().getColor(isExposed ? R.color.status_blue : R.color.status_green_bg));

			TracingStatusHelper.updateStatusView(statusView, state, title, text);
			statusBubble.setBackgroundTintList(bubbleColor);
			statusBubbleTriangle.setImageTintList(bubbleColor);
			infoBubbleExposed
					.setBackground(isExposed ? getResources().getDrawable(R.drawable.bg_status_bubble_stroke_grey, null)
											 : null);
			exposedInfoGroup.setVisibility(isExposed ? View.VISIBLE : View.GONE);
			if (isExposed) {
				((TextView) exposedInfoGroup.findViewById(R.id.notifications_info_text_specific)).setText(
						selfOrContactExposed.first ? R.string.meldungen_hinweis_info_text1_infected
												   : R.string.meldungen_hinweis_info_text1);
			}
		});
		 */
	}

	private class ReportsSlidePageAdapter extends FragmentStateAdapter {

		public ReportsSlidePageAdapter() {
			super(ReportsFragment.this);
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {
			switch (position) {
				case 0:
					return ReportsPagerFragment.newNoReportsInstance(ReportsPagerFragment.Type.NO_REPORTS);
				case 1:
					return ReportsPagerFragment.newNoReportsInstance(ReportsPagerFragment.Type.POSSIBLE_INFECTION);
				case 2:
					return ReportsPagerFragment.newNoReportsInstance(ReportsPagerFragment.Type.NEW_CONTACT);
			}
			throw new IllegalArgumentException("There is no fragment for view pager position " + position);
		}

		@Override
		public int getItemCount() {
			return 3;
		}

	}

}
