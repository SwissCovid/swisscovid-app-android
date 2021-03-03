package ch.admin.bag.dp3t.util;

import android.content.Context;

import java.util.Locale;

import ch.admin.bag.dp3t.R;

public class TranslationUtil {

	public static String getCountryName(Context context, String countryCode) {
		Locale countryLocale = new Locale("", countryCode);
		Locale langLocale = new Locale(context.getString(R.string.language_key));
		return countryLocale.getDisplayCountry(langLocale);
	}

}
