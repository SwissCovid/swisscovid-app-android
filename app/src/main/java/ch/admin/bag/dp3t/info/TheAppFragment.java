/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
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