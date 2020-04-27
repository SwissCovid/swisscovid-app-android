/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.html;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.dpppt.android.app.R;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HtmlFragment extends Fragment {

	private static final String ARG_BASE_URL = "ARG_BASE_URL";
	private static final String ARG_DATA = "ARG_DATA";
	private String baseUrl;
	private String data;
	private View loadingSpinner;
	private Toast downloadingFileToast;

	public static HtmlFragment newInstance(String baseUrl, @Nullable String data) {
		Bundle args = new Bundle();
		args.putString(ARG_BASE_URL, baseUrl);
		args.putString(ARG_DATA, data);
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
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
				if (url.toLowerCase().endsWith(".pdf")) {
					openFileFromUrl(url, "application/pdf");
					downloadingFileToast = Toast.makeText(getContext(), R.string.downloading_pdf, Toast.LENGTH_LONG);
					downloadingFileToast.show();
				} else if (url.toLowerCase().endsWith(".xlsx")) {
					openFileFromUrl(url, "text/csv");
					downloadingFileToast = Toast.makeText(getContext(), R.string.downloading_csv, Toast.LENGTH_LONG);
					downloadingFileToast.show();
				} else {
					Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(webIntent);
					return true;
				}
				return false;
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
					downloadingFileToast.cancel();
				});
	}

}
