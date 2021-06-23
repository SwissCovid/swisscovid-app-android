/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.travel;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.UiUtils;

public class TravelFragment extends Fragment {

	private static final String TAG = "TravelFragment";

	private SecureStorage secureStorage;

	private View headerView;
	private ScrollView scrollView;

	private LinearLayout countryList;

	public static TravelFragment newInstance() {
		return new TravelFragment();
	}

	public TravelFragment() { super(R.layout.fragment_travel); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		secureStorage = SecureStorage.getInstance(getContext());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.travel_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		headerView = view.findViewById(R.id.travel_header_view);
		scrollView = view.findViewById(R.id.travel_scroll_view);
		setupScrollBehavior();

		countryList = view.findViewById(R.id.travel_country_list);

		setupCountryList();
	}

	private void setupCountryList() {
		List<String> countries = secureStorage.getInteropCountries();

		for (String countryCode : countries) {
			String countryName = TravelUtils.getCountryName(requireContext(), countryCode);

			View countryItemView = getLayoutInflater().inflate(R.layout.item_travel_country, null);
			countryItemView.setContentDescription(countryName);

			ImageView flagImageView = countryItemView.findViewById(R.id.flag_icon);
			TextView flagTextView = countryItemView.findViewById(R.id.flag_cc);
			String idName = "flag_" + countryCode.toLowerCase(Locale.GERMAN);
			int drawableRes = UiUtils.getDrawableResourceByName(requireContext(), idName);
			if (drawableRes != 0) {
				flagImageView.setImageResource(drawableRes);
				flagTextView.setVisibility(View.GONE);
			} else {
				flagImageView.setVisibility(View.GONE);
				flagTextView.setText(countryCode);
			}

			TextView countryNameView = countryItemView.findViewById(R.id.travel_country_name);
			countryNameView.setText(countryName);

			countryList.addView(countryItemView);
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
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

}
