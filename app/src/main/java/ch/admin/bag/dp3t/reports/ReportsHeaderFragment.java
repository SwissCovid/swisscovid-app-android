/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.reports;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Iterator;
import java.util.List;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.DateUtils;

public class ReportsHeaderFragment extends Fragment {

	private static final String ARG_TYPE = "ARG_TYPE";
	private static final String ARG_TIMESTAMPS = "ARG_TIMESTAMPS";
	private static final String ARG_SHOWANIMATIONCONTROLS = "ARG_SHOWANIMATIONCONTROLS";

	public static ReportsHeaderFragment newInstance(@NonNull Type type, List<Long> timestamps, boolean showAnimationControls) {
		ReportsHeaderFragment reportsHeaderFragment = new ReportsHeaderFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TYPE, type.ordinal());
		args.putLongArray(ARG_TIMESTAMPS, toLongArray(timestamps));
		args.putBoolean(ARG_SHOWANIMATIONCONTROLS, showAnimationControls);
		reportsHeaderFragment.setArguments(args);
		return reportsHeaderFragment;
	}

	public enum Type {
		NO_REPORTS,
		POSSIBLE_INFECTION,
		POSITIVE_TESTED
	}


	private Type type;
	private long[] timestamps;
	private boolean showAnimationControls;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		type = Type.values()[getArguments().getInt(ARG_TYPE)];
		timestamps = getArguments().getLongArray(ARG_TIMESTAMPS);
		showAnimationControls = getArguments().getBoolean(ARG_SHOWANIMATIONCONTROLS);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View view = null;

		switch (type) {
			case NO_REPORTS:
				view = inflater.inflate(R.layout.fragment_reports_header_no_reports, container, false);
				break;
			case POSSIBLE_INFECTION:
				view = inflater.inflate(R.layout.fragment_reports_header_possible_infection, container, false);
				break;
			case POSITIVE_TESTED:
				view = inflater.inflate(R.layout.fragment_reports_header_positive_tested, container, false);
				break;
		}

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

		//TODO: What if timestamps is null or empty
		if (type == Type.POSSIBLE_INFECTION) {
			TextView date = view.findViewById(R.id.fragment_reports_header_date);
			View info = view.findViewById(R.id.fragment_reports_header_info);
			View image = view.findViewById(R.id.fragment_reports_header_image);
			Button continueButton = view.findViewById(R.id.fragment_reports_header_continue_button);
			TextView subtitle = view.findViewById(R.id.fragment_reports_header_subtitle);
			TextView showAllButton = view.findViewById(R.id.fragment_reports_header_show_all_button);
			ViewGroup daysContainer = view.findViewById(R.id.fragment_reports_dates_container);

			if (date != null) {
				date.setText(getDateString(timestamps[0], true));
			}

			daysContainer.removeAllViews();
			for (long timestamp : timestamps) {
				View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_reports_exposure_day, daysContainer,
						false);
				TextView itemDate = itemView.findViewById(R.id.exposure_day_textview);
				itemDate.setText(getDateString(timestamp, false));
				daysContainer.addView(itemView);
			}
			if (timestamps.length <= 1) {
				showAllButton.setVisibility(View.GONE);
			} else {
				showAllButton.setVisibility(View.VISIBLE);
			}

			showAllButton.setOnClickListener(view1 -> {
				if (daysContainer.getVisibility() == View.VISIBLE) {
					subtitle.setText(R.string.meldung_detail_exposed_subtitle_last_encounter);
					showAllButton.setText(R.string.meldung_detail_exposed_show_all_button);
					((ReportsFragment) getParentFragment()).animateHeaderHeight(false, timestamps.length, daysContainer, date);
				} else {
					subtitle.setText(R.string.meldung_detail_exposed_subtitle_all_encounters);
					showAllButton.setText(R.string.meldung_detail_exposed_show_less_button);
					((ReportsFragment) getParentFragment()).animateHeaderHeight(true, timestamps.length, daysContainer, date);
				}
			});
			showAllButton.setPaintFlags(showAllButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

			if (showAnimationControls) {

				info.setVisibility(View.GONE);
				showAllButton.setVisibility(View.GONE);
				image.setVisibility(View.VISIBLE);
				continueButton.setVisibility(View.VISIBLE);

				continueButton.setOnClickListener(view1 -> {
					((ReportsFragment) getParentFragment())
							.doHeaderAnimation(info, image, continueButton, showAllButton, timestamps.length);
				});
			}
		}
	}

	private String getDateString(long timestamp, boolean withDiff) {
		String dateStr = DateUtils.getFormattedDate(timestamp);
		if (!withDiff) return dateStr;

		dateStr += " / ";
		int daysDiff = DateUtils.getDaysDiff(timestamp);

		if (daysDiff == 0) {
			dateStr += getString(R.string.date_today);
		} else if (daysDiff == 1) {
			dateStr += getString(R.string.date_one_day_ago);
		} else {
			dateStr += getString(R.string.date_days_ago).replace("{COUNT}", String.valueOf(daysDiff));
		}
		return dateStr;
	}

	private static long[] toLongArray(List<Long> longs) {
		if (longs == null) return null;
		long[] ret = new long[longs.size()];
		Iterator<Long> iterator = longs.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next();
		}
		return ret;
	}

}
