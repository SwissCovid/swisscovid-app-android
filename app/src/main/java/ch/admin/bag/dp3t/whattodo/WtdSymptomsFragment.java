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
		view.findViewById(R.id.wtd_symtoms_button).setOnClickListener(v -> showWhereToTestDialog());
	}

	private void showWhereToTestDialog() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.add(WhereToTestDialogFragment.newInstance(), WhereToTestDialogFragment.class.getCanonicalName())
				.commit();
	}

}
