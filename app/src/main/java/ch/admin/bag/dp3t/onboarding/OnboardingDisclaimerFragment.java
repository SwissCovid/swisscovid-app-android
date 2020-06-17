/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;

public class OnboardingDisclaimerFragment extends Fragment {

	public static OnboardingDisclaimerFragment newInstance() {
		return new OnboardingDisclaimerFragment();
	}

	public OnboardingDisclaimerFragment() {
		super(R.layout.fragment_onboarding_disclaimer);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));
		String versionText = getString(R.string.onboarding_disclaimer_app_version) + " " + BuildConfig.VERSION_NAME + "\n" +
				getString(R.string.onboarding_disclaimer_release_version) + " " + sdf.format(BuildConfig.BUILD_TIME);
		TextView versionInfo = view.findViewById(R.id.onboarding_disclaimer_version_info);
		versionInfo.setText(versionText);

		view.findViewById(R.id.onboarding_disclaimer_conditions_button).setOnClickListener(v -> {
			Intent browserIntent =
					new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.onboarding_disclaimer_legal_button_url)));
			startActivity(browserIntent);
		});

		Button continueButton = view.findViewById(R.id.onboarding_continue_button);
		continueButton.setOnClickListener(v -> ((OnboardingActivity) getActivity()).continueToNextPage());
	}

}
