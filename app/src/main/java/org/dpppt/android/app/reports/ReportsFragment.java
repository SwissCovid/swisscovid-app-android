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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dpppt.android.app.R;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.sdk.internal.database.models.MatchedContact;

public class ReportsFragment extends Fragment {

	public static ReportsFragment newInstance() {
		return new ReportsFragment();
	}

	private TracingViewModel tracingViewModel;

	private ReportsSlidePageAdapter pagerAdapter;

	private NestedScrollableHost header;
	private ViewPager2 viewPager;
	private LockableScrollView scrollView;
	private View scrollViewFirstchild;
	private CirclePageIndicator circlePageIndicator;

	private View healthyView;
	private View exposedFirstView;
	private View exposed2ndView;
	private View infectedView;


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

		healthyView = view.findViewById(R.id.reports_healthy);
		exposedFirstView = view.findViewById(R.id.reports_exponsed_first);
		exposed2ndView = view.findViewById(R.id.reports_exponsed_second);
		infectedView = view.findViewById(R.id.reports_infected);

		viewPager.setAdapter(pagerAdapter);
		circlePageIndicator.setViewPager(viewPager);

		tracingViewModel.getTracingStatusLiveData().observe(getViewLifecycleOwner(), status -> {
			healthyView.setVisibility(View.GONE);
			exposedFirstView.setVisibility(View.GONE);
			exposed2ndView.setVisibility(View.GONE);
			infectedView.setVisibility(View.GONE);
			switch (status.getInfectionStatus()) {
				case HEALTHY:
					healthyView.setVisibility(View.VISIBLE);
					pagerAdapter.updateItems(Arrays.asList(ReportsPagerFragment.Type.NO_REPORTS));
					break;
				case EXPOSED:
					//TODO für KONsti
					boolean hasCalled = false;
					if (hasCalled) {
						exposed2ndView.setVisibility(View.VISIBLE);
					} else {
						exposedFirstView.setVisibility(View.VISIBLE);
					}
					if (status.getMatchedContacts().size() == 1) {
						pagerAdapter.updateItems(Arrays.asList(ReportsPagerFragment.Type.POSSIBLE_INFECTION));
					} else {
						List<ReportsPagerFragment.Type> items = new ArrayList<>();
						for (int i = 0; i < status.getMatchedContacts().size(); i++) {
							MatchedContact matchedContact = status.getMatchedContacts().get(i);
							if (i == 0) {
								items.add(ReportsPagerFragment.Type.POSSIBLE_INFECTION);
							} else {
								items.add(ReportsPagerFragment.Type.NEW_CONTACT);
							}
						}
						pagerAdapter.updateItems(items);
					}

					break;
				case INFECTED:
					infectedView.setVisibility(View.VISIBLE);
					pagerAdapter.updateItems(Arrays.asList(ReportsPagerFragment.Type.POSITIVE_TESTED));
					break;
			}
		});
	}

	private class ReportsSlidePageAdapter extends FragmentStateAdapter {

		private List<ReportsPagerFragment.Type> items = new ArrayList<>();

		ReportsSlidePageAdapter() {
			super(ReportsFragment.this);
		}

		void updateItems(List<ReportsPagerFragment.Type> items) {
			this.items.clear();
			this.items.addAll(items);
			notifyDataSetChanged();

			if (getItemCount() > 1) {
				circlePageIndicator.setVisibility(View.VISIBLE);
				ViewGroup.LayoutParams lp = header.getLayoutParams();
				lp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports_with_indicator);
				header.setLayoutParams(lp);
				scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
						getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports_width_indicator),
						scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
			} else {
				circlePageIndicator.setVisibility(View.GONE);
				ViewGroup.LayoutParams lp = header.getLayoutParams();
				lp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports);
				header.setLayoutParams(lp);
				scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
						getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports),
						scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
			}

			viewPager.post(() -> {
				Rect rect = new Rect();
				viewPager.getDrawingRect(rect);
				scrollView.setScrollPreventRect(rect);
			});
		}

		@NonNull
		@Override
		public Fragment createFragment(int position) {

			ReportsPagerFragment.Type type = items.get(position);

			switch (type) {
				case NO_REPORTS:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.NO_REPORTS);
				case POSSIBLE_INFECTION:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.POSSIBLE_INFECTION);
				case NEW_CONTACT:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.NEW_CONTACT);
				case POSITIVE_TESTED:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.POSITIVE_TESTED);
			}

			throw new IllegalArgumentException();
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

	}

}
