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

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.dpppt.android.sdk.DP3T;

import ch.admin.bag.dp3t.R;

public class InformActivity extends FragmentActivity {

	private boolean backpressAllowed = true;
	public static final String EXTRA_COVIDCODE = "EXTRA_COVIDCODE";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inform);

		if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.inform_fragment_container, InformIntroFragment.newInstance())
					.commit();
		}
	}

	@Override
	public void onBackPressed() {
		if (backpressAllowed) {
			super.onBackPressed();
		}
	}

	public void allowBackButton(boolean allowed) {
		this.backpressAllowed = allowed;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DP3T.onActivityResult(this, requestCode, resultCode, data);
	}

}
