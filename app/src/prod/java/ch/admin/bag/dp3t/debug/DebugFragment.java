/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.debug;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class DebugFragment extends Fragment {

	/**
	 * DOES NOT (AND MUST NOT) EXIST ON PROD.
	 */
	public static final boolean EXISTS = false;

	public static void startDebugFragment(FragmentManager parentFragmentManager) { }

}
