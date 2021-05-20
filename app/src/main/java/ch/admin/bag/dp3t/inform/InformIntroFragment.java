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
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.fragment.app.Fragment;

import java.util.List;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.travel.TravelUtils;

public class InformIntroFragment extends Fragment {

	public static InformIntroFragment newInstance() {
		return new InformIntroFragment();
	}

	public InformIntroFragment() {
		super(R.layout.fragment_inform_intro);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Button cancelButton = view.findViewById(R.id.inform_intro_cancel_button);
		cancelButton.setOnClickListener(v -> {
			getActivity().finish();
		});
		((InformActivity) requireActivity()).allowBackButton(true);

		SecureStorage secureStorage = SecureStorage.getInstance(getContext());
		List<String> countries = secureStorage.getInteropCountries();
		ViewGroup countriesContainer = view.findViewById(R.id.inform_intro_travel);
		if (!countries.isEmpty()) {
			countriesContainer.setVisibility(View.VISIBLE);
			Flow flowConstraint = view.findViewById(R.id.inform_intro_travel_flags_flow);
			TravelUtils.inflateFlagFlow(flowConstraint, countries);
		} else {
			countriesContainer.setVisibility(View.GONE);
		}

		Button continueButton = view.findViewById(R.id.inform_intro_button_continue);
		continueButton.setOnClickListener(v -> {
			getParentFragmentManager()
					.beginTransaction()
					.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
					.replace(R.id.inform_fragment_container, InformFragment.newInstance())
					.addToBackStack(InformFragment.class.getCanonicalName())
					.commit();
		});
	}

}
