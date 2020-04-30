/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dpppt.android.app.BuildConfig;
import org.dpppt.android.app.R;

public class AssetUtil {

	private static final String PREFIX_ASSET_FILE = "file:///android_asset/";
	private static final String FOLDER_NAME_IMPRESSUM = "impressum/";
	private static final String FILE_NAME_IMPRESSUM = "impressum.html";

	private static final String REPLACE_STRING_VERSION = "{VERSION}";
	private static final String REPLACE_STRING_BUILDNR = "{BUILDNR}";

	public static String getImpressumBaseUrl(Context context) {
		return PREFIX_ASSET_FILE + getFolderNameImpressum(context);
	}

	public static String getFolderNameImpressum(Context context) {
		return FOLDER_NAME_IMPRESSUM + context.getString(R.string.language_key) + "/";
	}

	public static String getImpressumHtml(Context context) {
		return loadImpressumHtmlFile(context, FILE_NAME_IMPRESSUM);
	}

	public static String loadImpressumHtmlFile(Context context, String filename) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open(getFolderNameImpressum(context) + filename)));
			StringBuilder html = new StringBuilder();
			for (String line; (line = reader.readLine()) != null; ) {
				html.append(line);
			}
			String impressum = html.toString();
			String version = BuildConfig.VERSION_NAME + ", " + org.dpppt.android.sdk.BuildConfig.VERSION_NAME;
			String buildDate = SimpleDateFormat.getDateTimeInstance().format(new Date(BuildConfig.BUILD_TIME));
			impressum = impressum.replace(REPLACE_STRING_VERSION, version);
			impressum = impressum.replace(REPLACE_STRING_BUILDNR, buildDate);
			return impressum;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

}
