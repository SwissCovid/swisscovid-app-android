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
import android.content.Intent;
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
import ch.admin.bag.dp3t.inform.InformActivity;
import ch.admin.bag.dp3t.networking.models.FaqEntryModel;
import ch.admin.bag.dp3t.networking.models.InfoBoxModel;
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

		view.findViewById(R.id.wtd_inform_button).setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), InformActivity.class);
			startActivity(intent);
		});

		view.findViewById(R.id.wtd_inform_faq_button).setOnClickListener(v -> {
			UrlUtil.openUrl(getContext(), getString(R.string.faq_button_url));
		});

		View oldCallButton = view.findViewById(R.id.wtd_inform_call_infoline_coronavirus);
		if (oldCallButton != null) {
			oldCallButton.setOnClickListener(v -> {
				PhoneUtil.callInfolineCoronavirus(v.getContext());
			});
		}
	}

	private void fillContentFromConfigServer(View view) {
		Context context = view.getContext();
		SecureStorage secureStorage = SecureStorage.getInstance(context);
		WhatToDoPositiveTestTextsModel textModel =
				secureStorage.getWhatToDoPositiveTestTexts(context.getString(R.string.language_key));

		if (textModel != null) {
			((TextView) view.findViewById(R.id.wtd_inform_box_supertitle)).setText(textModel.getEnterCovidcodeBoxSupertitle());
			((TextView) view.findViewById(R.id.wtd_inform_box_title)).setText(textModel.getEnterCovidcodeBoxTitle());
			((TextView) view.findViewById(R.id.wtd_inform_box_text)).setText(textModel.getEnterCovidcodeBoxText());
			((TextView) view.findViewById(R.id.wtd_inform_button)).setText(textModel.getEnterCovidcodeBoxButtonTitle());
			InfoBoxModel infoBox = textModel.getInfoBox();

			if (infoBox != null) {
				view.findViewById(R.id.wtd_inform_infobox).setVisibility(View.VISIBLE);
				((TextView) view.findViewById(R.id.wtd_inform_infobox_title)).setText(infoBox.getTitle());
				((TextView) view.findViewById(R.id.wtd_inform_infobox_msg)).setText(infoBox.getMsg());

				if (infoBox.getUrl() != null && infoBox.getUrlTitle() != null) {
					((TextView) view.findViewById(R.id.wtd_inform_infobox_link_text)).setText(infoBox.getUrlTitle());
					view.findViewById(R.id.wtd_inform_infobox_link_layout).setOnClickListener(v -> {
						UrlUtil.openUrl(v.getContext(), infoBox.getUrl());
					});
					view.findViewById(R.id.wtd_inform_infobox_link_layout).setVisibility(View.VISIBLE);
					ImageView linkIcon = view.findViewById(R.id.wtd_inform_infobox_link_icon);
					if (infoBox.getUrl().startsWith("tel://")) {
						linkIcon.setImageResource(R.drawable.ic_phone);
					} else {
						linkIcon.setImageResource(R.drawable.ic_launch);
					}
				} else {
					view.findViewById(R.id.wtd_inform_infobox_link_layout).setVisibility(View.GONE);
				}

				if (infoBox.getHearingImpairedInfo() != null) {
					((ImageView) view.findViewById(R.id.wtd_inform_infobox_link_icon)).setImageResource(R.drawable.ic_phone);
					view.findViewById(R.id.wtd_inform_infobox_link_hearing_impaired).setOnClickListener(v -> {
						requireActivity().getSupportFragmentManager().beginTransaction()
								.add(WtdInfolineAccessabilityDialogFragment.newInstance(infoBox.getHearingImpairedInfo()),
										WtdInfolineAccessabilityDialogFragment.class.getCanonicalName())
								.commit();
					});
					view.findViewById(R.id.wtd_inform_infobox_link_hearing_impaired).setVisibility(View.VISIBLE);
				} else {
					((ImageView) view.findViewById(R.id.wtd_inform_infobox_link_icon)).setImageResource(R.drawable.ic_launch);
					view.findViewById(R.id.wtd_inform_infobox_link_hearing_impaired).setVisibility(View.GONE);
				}
			} else {
				view.findViewById(R.id.wtd_inform_infobox).setVisibility(View.GONE);
			}

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
