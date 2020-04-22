/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.info;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.R;

public class TheAppFragment extends Fragment {

	public static TheAppFragment newInstance() {
		return new TheAppFragment();
	}

	public TheAppFragment() {
		super(R.layout.fragment_the_app);
	}

}
