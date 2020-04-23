/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.inform;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.dpppt.android.app.R;

public class ThankYouFragment extends Fragment {

	public static ThankYouFragment newInstance() {
		return new ThankYouFragment();
	}

	public ThankYouFragment() {
		super(R.layout.fragment_thank_you);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.inform_thank_you_button_continue).setOnClickListener(v -> {
			getParentFragmentManager().beginTransaction()
					.replace(R.id.inform_fragment_container, GetWellFragment.newInstance())
					.commit();
		});
	}



}
