/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import androidx.annotation.NonNull;

public class UiUtils {

	public static int dpToPx(@NonNull Resources resources, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
	}

	public static float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

	public static int getDrawableResourceByName(Context context, String idName) {
		return context.getResources().getIdentifier(idName, "drawable", context.getPackageName());
	}

}