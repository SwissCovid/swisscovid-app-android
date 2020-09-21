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
import ch.admin.bag.dp3t.inform.InformActivity;
import ch.admin.bag.dp3t.util.PhoneUtil;
import ch.admin.bag.dp3t.util.UrlUtil;

public class WtdPositiveTestFragment extends Fragment {

	public static WtdPositiveTestFragment newInstance() {
		return new WtdPositiveTestFragment();
	}

	public WtdPositiveTestFragment() {
		super(R.layout.fragment_what_to_do_test);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.wtd_test_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		view.findViewById(R.id.wtd_inform_button).setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), InformActivity.class);
			startActivity(intent);
		});

		view.findViewById(R.id.wtd_inform_faq_button).setOnClickListener(v -> {
			UrlUtil.openUrl(getContext(), getString(R.string.faq_button_url));
		});

		view.findViewById(R.id.wtd_inform_call_infoline_coronavirus).setOnClickListener(v->{
			PhoneUtil.callInfolineCoronavirus(v.getContext());
		});

	}

}
