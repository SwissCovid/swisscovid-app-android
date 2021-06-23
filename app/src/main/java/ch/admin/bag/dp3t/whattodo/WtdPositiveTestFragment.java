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

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.models.FaqEntryModel;
import ch.admin.bag.dp3t.networking.models.WhatToDoPositiveTestTextsModel;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.PhoneUtil;
import ch.admin.bag.dp3t.util.UrlUtil;

public class WtdPositiveTestFragment extends Fragment {

	public static WtdPositiveTestFragment newInstance() {
		return new WtdPositiveTestFragment();
	}

	public WtdPositiveTestFragment() {
		super(R.layout.fragment_what_to_do_test);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.wtd_test_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		fillContentFromConfigServer(view);

		view.findViewById(R.id.wtd_inform_faq_button).setOnClickListener(v -> {
			UrlUtil.openUrl(getContext(), getString(R.string.faq_button_url));
		});
	}

	private void fillContentFromConfigServer(View view) {
		Context context = view.getContext();
		SecureStorage secureStorage = SecureStorage.getInstance(context);
		WhatToDoPositiveTestTextsModel textModel =
				secureStorage.getWhatToDoPositiveTestTexts(context.getString(R.string.language_key));

		if (textModel != null) {
			LinearLayout faqLayout = view.findViewById(R.id.wtd_inform_faq_layout);
			faqLayout.removeAllViews();

			if (textModel.getFaqEntries() != null) {
				for (FaqEntryModel faqEntry : textModel.getFaqEntries()) {

					View itemView = getLayoutInflater().inflate(R.layout.item_faq, faqLayout, false);
					((TextView) itemView.findViewById(R.id.item_faq_title)).setText(faqEntry.getTitle());
					((TextView) itemView.findViewById(R.id.item_faq_text)).setText(faqEntry.getText());

					int iconResource =
							context.getResources().getIdentifier(faqEntry.getIconAndroid(), "drawable", context.getPackageName());
					if (iconResource != 0) {
						((ImageView) itemView.findViewById(R.id.item_faq_icon)).setImageResource(iconResource);
					}

					if (faqEntry.getLinkUrl() != null && faqEntry.getLinkTitle() != null) {
						((TextView) itemView.findViewById(R.id.item_faq_link_text)).setText(faqEntry.getLinkTitle());

						itemView.findViewById(R.id.item_faq_link_layout).setOnClickListener(v -> {
							UrlUtil.openUrl(v.getContext(), faqEntry.getLinkUrl());
						});
					} else {
						itemView.findViewById(R.id.item_faq_link_layout).setVisibility(View.GONE);
					}
					faqLayout.addView(itemView);
				}
			}
		}
	}

}
