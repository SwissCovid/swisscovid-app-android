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

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.AccessibilityUtil;
import ch.admin.bag.dp3t.util.AssetUtil;
import ch.admin.bag.dp3t.util.UlTagHandler;
import ch.admin.bag.dp3t.util.UrlUtil;

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

		TextView termsOfUseTextview = view.findViewById(R.id.terms_of_use_textview);
		TextView dataProtectionTextView = view.findViewById(R.id.data_protection_textview);
		termsOfUseTextview.setText(Html.fromHtml(AssetUtil.getTermsOfUse(getContext()), null, new UlTagHandler()));
		dataProtectionTextView.setText(Html.fromHtml(AssetUtil.getDataProtection(getContext()), null, new UlTagHandler()));

		ImageView termsOfUseChevron = view.findViewById(R.id.terms_of_use_chevron_imageview);
		ImageView dataProtectionChevron = view.findViewById(R.id.data_protection_chevron_imageview);

		View dataProtectionToOnlineVersionButton =
				view.findViewById(R.id.onboarding_disclaimer_data_protection_to_online_version_button);
		View termsOfUseToOnlineVersionButton = view.findViewById(R.id.onboarding_disclaimer_terms_of_use_to_online_version_button);

		View termsOfUseHeader = view.findViewById(R.id.conditions_of_use_header_container);
		View termsOfUseContainer = view.findViewById(R.id.onboarding_disclaimer_terms_of_use_container);
		View dataProtectionHeader = view.findViewById(R.id.data_protection_header_container);
		View dataProtectionContainer = view.findViewById(R.id.onboarding_disclaimer_data_protection_container);

		AccessibilityUtil.setExpansionToggleStateAccessibilityDelegate(dataProtectionHeader, dataProtectionContainer);
		dataProtectionHeader.setOnClickListener(v -> {
			if (dataProtectionContainer.getVisibility() == View.VISIBLE) {
				dataProtectionContainer.setVisibility(View.GONE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
			} else {
				dataProtectionContainer.setVisibility(View.VISIBLE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_light));
			}
			dataProtectionChevron.animate()
					.rotation(dataProtectionChevron.getRotation() + 180)
					.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
					.start();
		});

		AccessibilityUtil.setExpansionToggleStateAccessibilityDelegate(termsOfUseHeader, termsOfUseContainer);
		termsOfUseHeader.setOnClickListener(v -> {
			if (termsOfUseContainer.getVisibility() == View.VISIBLE) {
				termsOfUseContainer.setVisibility(View.GONE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
			} else {
				termsOfUseContainer.setVisibility(View.VISIBLE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_light));
			}
			termsOfUseChevron.animate()
					.rotation(termsOfUseChevron.getRotation() + 180)
					.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
					.start();
		});

		dataProtectionToOnlineVersionButton.setOnClickListener(v -> openOnlineVersion());
		AccessibilityUtil.setButtonAccessibilityDelegate(dataProtectionToOnlineVersionButton);
		termsOfUseToOnlineVersionButton.setOnClickListener(v -> openOnlineVersion());
		AccessibilityUtil.setButtonAccessibilityDelegate(termsOfUseToOnlineVersionButton);

		Button continueButton = view.findViewById(R.id.onboarding_continue_button);
		continueButton.setOnClickListener(v -> ((OnboardingActivity) getActivity()).continueToNextPage());
	}

	private void openOnlineVersion() {
		UrlUtil.openUrl(getContext(), getString(R.string.onboarding_disclaimer_legal_button_url));
	}

}
