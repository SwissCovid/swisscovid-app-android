package org.dpppt.android.app.util;

import android.content.res.Resources;
import android.util.TypedValue;
import androidx.annotation.NonNull;

public class UiUtils {

	public static int dpToPx(@NonNull Resources resources, int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
	}

}