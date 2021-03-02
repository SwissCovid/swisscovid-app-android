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

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import ch.admin.bag.dp3t.R;

public class StatsDetailsDialogFragment extends DialogFragment {

	private static final String ARG_TINT_COLOR = "ARG_TINT_COLOR";
	private static final String ARG_TITLE = "ARG_TITLE";
	private static final String ARG_SUBTITLE = "ARG_SUBTITLE";
	private static final String ARG_SECTIONS = "ARG_SECTIONS";

	@ColorRes private int tintColorResId;
	private String title;
	private String subtitle;
	private ArrayList<StatsDetailsSection> sections;

	public static StatsDetailsDialogFragment newInstance(
			@ColorRes int tintColorResId,
			String title,
			String subtitle,
			ArrayList<StatsDetailsSection> sections
	) {
		StatsDetailsDialogFragment fragment = new StatsDetailsDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TINT_COLOR, tintColorResId);
		args.putString(ARG_TITLE, title);
		args.putString(ARG_SUBTITLE, subtitle);
		args.putParcelableArrayList(ARG_SECTIONS, sections);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tintColorResId = getArguments().getInt(ARG_TINT_COLOR, R.color.blue_main);
		title = getArguments().getString(ARG_TITLE, "");
		subtitle = getArguments().getString(ARG_SUBTITLE, "");
		sections = getArguments().getParcelableArrayList(ARG_SECTIONS);
	}

	@Override
	public void onResume() {
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		super.onResume();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_stats_details, container);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		int tintColor = ContextCompat.getColor(view.getContext(), tintColorResId);

		ImageView closeButton = view.findViewById(R.id.stats_details_info_close_button);
		closeButton.setImageTintList(ColorStateList.valueOf(tintColor));
		closeButton.setOnClickListener(v -> dismiss());

		TextView titleView = view.findViewById(R.id.stats_details_title);
		titleView.setText(title);

		TextView subtitleView = view.findViewById(R.id.stats_details_subtitle);
		subtitleView.setText(subtitle);
		subtitleView.setTextColor(tintColor);

		ViewGroup sectionsContainer = view.findViewById(R.id.stats_details_sections);
		sectionsContainer.removeAllViews();

		LayoutInflater inflater = LayoutInflater.from(view.getContext());
		for (StatsDetailsSection section : sections) {
			View sectionView = inflater.inflate(R.layout.item_stats_details_section, sectionsContainer, false);
			((TextView) sectionView.findViewById(R.id.stats_details_section_title)).setText(section.getSectionTitle());
			((TextView) sectionView.findViewById(R.id.stats_details_section_content)).setText(section.getSectionContent());
			sectionsContainer.addView(sectionView);
		}
	}

}

