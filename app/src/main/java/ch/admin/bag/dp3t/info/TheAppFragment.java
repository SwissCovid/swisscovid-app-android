/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.info;

import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;

public class TheAppFragment extends Fragment {

	public static TheAppFragment newInstance() {
		return new TheAppFragment();
	}

	public TheAppFragment() {
		super(R.layout.fragment_the_app);
	}

}