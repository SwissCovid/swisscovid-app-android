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

public class AssetUtil {

	private static final String PREFIX_ASSET_FILE = "file:///android_asset/";
	private static final String FOLDER_NAME_IMPRESSUM = "impressum/";
	private static final String FILE_NAME_IMPRESSUM = "impressum.html";

	public static String getImpressumBaseUrl() {
		return PREFIX_ASSET_FILE + FOLDER_NAME_IMPRESSUM;
	}

	public static String getImpressumHtml(Context context) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open(FOLDER_NAME_IMPRESSUM + FILE_NAME_IMPRESSUM)));
			StringBuilder html = new StringBuilder();
			for (String line; (line = reader.readLine()) != null; ) {
				html.append(line);
			}
			return html.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
