/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.html;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.AssetUtil;
import ch.admin.bag.dp3t.util.UrlUtil;

public class HtmlFragment extends Fragment {

	private static final String ARG_BASE_URL = "ARG_BASE_URL";
	private static final String ARG_DATA = "ARG_DATA";
	private static final String ARG_TITLE = "ARG_TITLE";

	private String baseUrl;
	private String data;
	@StringRes
	private int titleRes;
	private View loadingSpinner;

	public static HtmlFragment newInstance(int titleRes, String baseUrl, @Nullable String data) {
		Bundle args = new Bundle();
		args.putString(ARG_BASE_URL, baseUrl);
		args.putString(ARG_DATA, data);
		args.putInt(ARG_TITLE, titleRes);
		HtmlFragment fragment = new HtmlFragment();
		fragment.setArguments(args);
		return fragment;
	}

	private HtmlFragment() {
		super(R.layout.fragment_html);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseUrl = getArguments().getString(ARG_BASE_URL);
		data = getArguments().getString(ARG_DATA);
		titleRes = getArguments().getInt(ARG_TITLE);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Toolbar toolbar = view.findViewById(R.id.html_toolbar);
		toolbar.setTitle(titleRes);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		WebView web = view.findViewById(R.id.html_webview);
		loadingSpinner = getView().findViewById(R.id.loading_spinner);

		web.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				loadingSpinner.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (baseUrl.equals(url)) return true;
				if (url.toLowerCase().startsWith("dp3t://")) {
					String strippedUrl = url.substring(7);
					HtmlFragment htmlFragment =
							HtmlFragment.newInstance(R.string.menu_impressum, baseUrl,
									AssetUtil.loadImpressumHtmlFile(getContext(), strippedUrl));
					getParentFragmentManager().beginTransaction()
							.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter,
									R.anim.slide_pop_exit)
							.replace(R.id.main_fragment_container, htmlFragment)
							.addToBackStack(HtmlFragment.class.getCanonicalName())
							.commit();
					return true;
				}
				UrlUtil.openUrl(getContext(), url);
				return true;
			}
		});

		WebSettings webSettings = web.getSettings();
		webSettings.setJavaScriptEnabled(true);
		if (data != null) {
			web.loadDataWithBaseURL(baseUrl, data, "text/html", "UTF-8", null);
		} else {
			web.loadUrl(baseUrl);
		}
	}

}
