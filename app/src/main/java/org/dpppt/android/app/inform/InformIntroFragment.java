/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.inform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.R;

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

		Button continueButton = view.findViewById(R.id.inform_intro_button_continue);
		continueButton.setOnClickListener(v -> {
			getParentFragmentManager()
					.beginTransaction()
					.replace(R.id.inform_fragment_container, InformFragment.newInstance())
					.commit();
		});

	}

}
