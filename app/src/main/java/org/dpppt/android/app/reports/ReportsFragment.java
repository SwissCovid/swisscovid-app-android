/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.reports;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Pair;
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
import java.util.List;

import org.dpppt.android.app.R;
import org.dpppt.android.app.storage.SecureStorage;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.sdk.internal.database.models.MatchedContact;

public class ReportsFragment extends Fragment {

	public static ReportsFragment newInstance() {
		return new ReportsFragment();
	}

	private TracingViewModel tracingViewModel;
	private SecureStorage secureStorage;

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
		secureStorage = SecureStorage.getInstance(getContext());
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
			List<Pair<ReportsPagerFragment.Type, Long>> items = new ArrayList<>();
			switch (status.getInfectionStatus()) {
				case HEALTHY:
					healthyView.setVisibility(View.VISIBLE);
					items.add(new Pair<>(ReportsPagerFragment.Type.NO_REPORTS, null));
					break;
				case EXPOSED:
					List<MatchedContact> matchedContacts = status.getMatchedContacts();
					//TODO für KONsti
					boolean hasCalled = false;
					if (hasCalled) {
						exposed2ndView.setVisibility(View.VISIBLE);
					} else {
						exposedFirstView.setVisibility(View.VISIBLE);
					}
					for (int i = 0; i < matchedContacts.size(); i++) {
						MatchedContact matchedContact = matchedContacts.get(i);
						if (i == 0) {
							items.add(new Pair<>(ReportsPagerFragment.Type.POSSIBLE_INFECTION, matchedContact.getReportDate()));
						} else {
							items.add(new Pair<>(ReportsPagerFragment.Type.NEW_CONTACT, matchedContact.getReportDate()));
						}
					}
					break;
				case INFECTED:
					infectedView.setVisibility(View.VISIBLE);
					items.add(new Pair<>(ReportsPagerFragment.Type.POSITIVE_TESTED, secureStorage.getInfectedDate()));
					break;
			}
			pagerAdapter.updateItems(items);
		});
	}

	private class ReportsSlidePageAdapter extends FragmentStateAdapter {

		private List<Pair<ReportsPagerFragment.Type, Long>> items = new ArrayList<>();

		ReportsSlidePageAdapter() {
			super(ReportsFragment.this);
		}

		void updateItems(List<Pair<ReportsPagerFragment.Type, Long>> items) {
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

			Pair<ReportsPagerFragment.Type, Long> item = items.get(position);
			ReportsPagerFragment.Type type = item.first;
			long timestamp = item.second == null ? 0 : item.second;

			switch (type) {
				case NO_REPORTS:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.NO_REPORTS, 0);
				case POSSIBLE_INFECTION:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.POSSIBLE_INFECTION, timestamp);
				case NEW_CONTACT:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.NEW_CONTACT, timestamp);
				case POSITIVE_TESTED:
					return ReportsPagerFragment.newInstance(ReportsPagerFragment.Type.POSITIVE_TESTED, timestamp);
			}

			throw new IllegalArgumentException();
		}

		@Override
		public int getItemCount() {
			return items.size();
		}

	}

}
