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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import ch.admin.bag.dp3t.util.AssetUtil;
import ch.admin.bag.dp3t.R;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
							.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
							.replace(R.id.root_fragment_container, htmlFragment)
							.addToBackStack(HtmlFragment.class.getCanonicalName())
							.commit();
					return true;
				}
				if (url.toLowerCase().endsWith(".pdf")) {
					openFileFromUrl(url, "application/pdf");
				} else if (url.toLowerCase().endsWith(".xlsx")) {
					openFileFromUrl(url, "text/csv");
				} else {
					Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					if (webIntent.resolveActivity(getContext().getPackageManager()) != null) {
						startActivity(webIntent);
					}
				}
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

	private void openFileFromUrl(final String xlsUrl, String fileIntentType) {
		Activity activity = getActivity();
		loadingSpinner.setVisibility(View.VISIBLE);
		Single.fromCallable(() -> {
			try {
				String[] splitUrl = xlsUrl.split("/");
				URL url = new URL(xlsUrl);
				URLConnection connection = url.openConnection();
				connection.connect();

				InputStream input = new BufferedInputStream(connection.getInputStream());
				File dir = new File(activity.getFilesDir(), "/shared");
				dir.mkdir();
				File file = new File(dir, splitUrl[splitUrl.length - 1]);
				OutputStream output = new FileOutputStream(file);

				byte data[] = new byte[1024];
				int count;
				while ((count = input.read(data)) != -1) {
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
				return file;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(file -> {
					String authority = activity.getApplicationContext().getPackageName() + ".fileprovider";
					Uri uriToFile = FileProvider.getUriForFile(activity, authority, file);

					Intent shareIntent = new Intent(Intent.ACTION_VIEW);
					shareIntent.setDataAndType(uriToFile, fileIntentType);
					shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					if (shareIntent.resolveActivity(activity.getPackageManager()) != null) {
						activity.startActivity(shareIntent);
					}
					loadingSpinner.setVisibility(View.GONE);
				});
	}

}
