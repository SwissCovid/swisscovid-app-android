/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.whattodo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;

public class WtdSymptomsFragment extends Fragment {

	public static WtdSymptomsFragment newInstance() {
		return new WtdSymptomsFragment();
	}

	public WtdSymptomsFragment() {
		super(R.layout.fragment_what_to_do_symptoms);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.wtd_symptoms_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		view.findViewById(R.id.wtd_symptoms_check_button1).setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.symptom_detail_corona_check_url)));
			startActivity(browserIntent);
		});

		view.findViewById(R.id.wtd_symptoms_faq_button).setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.faq_button_url)));
			startActivity(browserIntent);
		});
	}

}
