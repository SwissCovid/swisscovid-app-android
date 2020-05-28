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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;

public class OnboardingPilotVersionFragment extends Fragment {

	public static OnboardingPilotVersionFragment newInstance() {
		return new OnboardingPilotVersionFragment();
	}

	public OnboardingPilotVersionFragment() {
		super(R.layout.fragment_onboarding_pilotversion);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Button continueButton = view.findViewById(R.id.onboarding_continue_button);
		continueButton.setOnClickListener(v -> showConfirmationDialog(v.getContext()));

		if (SecureStorage.getInstance(getContext()).isUserNotInPilotGroup()) {
			showGameOverDialog(getContext());
		}
	}

	private void showConfirmationDialog(Context context) {
		AlertDialog dialog = new AlertDialog.Builder(context, R.style.NextStep_AlertDialogStyle)
				.setTitle(R.string.onboarding_legal_alert_title)
				.setMessage(R.string.onboarding_legal_alert_message)
				.setPositiveButton(R.string.onboarding_legal_alert_yes, (dialog1, which) -> {
					dialog1.dismiss();
					continueOnboarding();
				})
				.setNegativeButton(R.string.onboarding_legal_alert_no, (dialog1, which) -> {
					dialog1.dismiss();
					SecureStorage.getInstance(context).setUserNotInPilotGroup(true);
					showGameOverDialog(context);
				})
				.create();
		dialog.setOnShowListener(dialog1 -> {
			TextView titleView = dialog.findViewById(R.id.alertTitle);
			titleView.setMaxLines(5);
		});
		dialog.show();
	}

	private void showGameOverDialog(Context context) {
		AlertDialog dialog = new AlertDialog.Builder(context, R.style.NextStep_AlertDialogStyle)
				.setTitle(R.string.onboarding_legal_blocker_title)
				.setMessage(R.string.onboarding_legal_blocker_message)
				.setOnDismissListener(dialog1 -> {
					abortOnboarding();
				})
				.show();
		dialog.setCanceledOnTouchOutside(false);
	}

	private void abortOnboarding() {
		((OnboardingActivity) getActivity()).abortOnboarding();
	}

	private void continueOnboarding() {
		((OnboardingActivity) getActivity()).continueToNextPage();
	}

}
