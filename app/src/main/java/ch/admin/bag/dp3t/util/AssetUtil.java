/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;

public class AssetUtil {

	private static final String PREFIX_ASSET_FILE = "file:///android_asset/";
	private static final String FOLDER_NAME_IMPRESSUM = "impressum/";
	private static final String FILE_NAME_IMPRESSUM = "impressum.html";

	private static final String REPLACE_STRING_VERSION = "{VERSION}";
	private static final String REPLACE_STRING_BUILDNR = "{BUILD}";

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
			StringBuilder versionString = new StringBuilder(BuildConfig.VERSION_NAME)
					.append(", ")
					.append(org.dpppt.android.sdk.BuildConfig.VERSION_NAME);
			StringBuilder buildString =
					new StringBuilder(SimpleDateFormat.getDateTimeInstance().format(new Date(BuildConfig.BUILD_TIME)))
							.append(" / ")
							.append(BuildConfig.FLAVOR);
					impressum = impressum.replace(REPLACE_STRING_VERSION, versionString);
			impressum = impressum.replace(REPLACE_STRING_BUILDNR, buildString);
			return impressum;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

}
