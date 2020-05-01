package org.dpppt.android.app.util;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import androidx.annotation.NonNull;

public class StringUtil {

	public static SpannableString makePartiallyBold(@NonNull String string, int start, int end) {
		SpannableString result = new SpannableString(string);
		result.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return result;
	}

}
