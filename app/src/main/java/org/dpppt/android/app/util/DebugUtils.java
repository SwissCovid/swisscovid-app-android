/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.util;

import org.dpppt.android.app.BuildConfig;

public class DebugUtils {

	public static boolean isDev() {
		return BuildConfig.IS_DEV;
	}

}
