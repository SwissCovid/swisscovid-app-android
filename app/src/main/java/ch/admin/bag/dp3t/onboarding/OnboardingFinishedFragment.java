/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.onboarding;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;

public class OnboardingFinishedFragment extends Fragment {

	public static OnboardingFinishedFragment newInstance() {
		return new OnboardingFinishedFragment();
	}

	public OnboardingFinishedFragment() {
		super(R.layout.fragment_onboarding_finished);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.onboarding_continue_button)
				.setOnClickListener(v -> {
					((OnboardingActivity) getActivity()).continueToNextPage();
				});
	}

}
