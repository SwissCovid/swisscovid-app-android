/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.whattodo;

import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.CustomStyleUrlSpan;

public class WtdInfolineAccessabilityDialogFragment extends DialogFragment {

	private static final String ARG_INFO_TEXT = "ARG_INFO_TEXT";

	private String infoText;

	public static WtdInfolineAccessabilityDialogFragment newInstance(String infoText) {
		Bundle args = new Bundle();
		args.putString(ARG_INFO_TEXT, infoText);

		WtdInfolineAccessabilityDialogFragment fragment = new WtdInfolineAccessabilityDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.infoText = getArguments().getString(ARG_INFO_TEXT);
	}

	@Override
	public void onResume() {
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		super.onResume();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_infoline_hearing_impaired, container);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.infoline_hearing_impaired_ok_button).setOnClickListener(v -> dismiss());
		TextView infoTextView = (TextView) view.findViewById(R.id.infoline_hearing_impaired_text);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			// The urlSpanFactory parameter type is only available starting with SDK 24
			Spannable spannable = new SpannableString(infoText);
			Linkify.addLinks(spannable, Linkify.EMAIL_ADDRESSES, s -> {
				CustomStyleUrlSpan span = new CustomStyleUrlSpan(s);
				span.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_main));
				span.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_bold));
				return span;
			});
			infoTextView.setText(spannable);
		} else {
			// On Android 6.0 we fall back to the regular Linkify logic, which does not make the span bold
			infoTextView.setText(infoText);
			Linkify.addLinks(infoTextView, Linkify.EMAIL_ADDRESSES);
			infoTextView.setLinkTextColor(ContextCompat.getColor(requireContext(), R.color.purple_main));
		}
		infoTextView.setMovementMethod(LinkMovementMethod.getInstance());
	}

}
