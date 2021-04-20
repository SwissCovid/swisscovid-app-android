/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.inform;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.TimeZone;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.DateUtils;

public class ThankYouFragment extends Fragment {

	private SecureStorage secureStorage;
	private final long MAX_EXPOSURE_AGE_MILLIS = 10 * 24 * 60 * 60 * 1000L;

	public static ThankYouFragment newInstance() {
		return new ThankYouFragment();
	}

	public ThankYouFragment() {
		super(R.layout.fragment_thank_you);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		secureStorage = SecureStorage.getInstance(getContext());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((InformActivity) requireActivity()).allowBackButton(false);

		// Show the onset date in the thank you message
		TextView thankYouInfoTextView = view.findViewById(R.id.inform_thank_you_text_info);
		TextView onsetDateTextView = view.findViewById(R.id.inform_thank_you_text_onsetdate);
		TextView stopInfectionChainsTextView = view.findViewById(R.id.inform_thank_you_text_stop_infection_chains);

		long oldestSharedKeyDateInMillis =
				Math.max(secureStorage.getPositiveReportOldestSharedKey(), System.currentTimeMillis() - MAX_EXPOSURE_AGE_MILLIS);
		if (oldestSharedKeyDateInMillis > 0L) {
			String formattedDate =
					DateUtils.getFormattedDateWrittenMonth(oldestSharedKeyDateInMillis, TimeZone.getTimeZone("UTC"));
			String formattedOnsetDateText =
					getString(R.string.inform_send_thankyou_text_onsetdate).replace("{ONSET_DATE}", formattedDate);

			thankYouInfoTextView.setText(R.string.inform_send_thankyou_text_onsetdate_info);
			onsetDateTextView.setText(formattedOnsetDateText);
			stopInfectionChainsTextView.setText(R.string.inform_send_thankyou_text_stop_infection_chains);
		} else {
			thankYouInfoTextView.setText(R.string.inform_send_thankyou_text);
			onsetDateTextView.setVisibility(View.GONE);
			stopInfectionChainsTextView.setVisibility(View.GONE);
		}

		view.findViewById(R.id.inform_thank_you_button_continue).setOnClickListener(v -> {
			getParentFragmentManager().beginTransaction()
					.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
					.replace(R.id.inform_fragment_container, TracingStoppedFragment.newInstance())
					.commit();
		});
	}

}
