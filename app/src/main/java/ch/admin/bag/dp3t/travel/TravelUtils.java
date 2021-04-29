package ch.admin.bag.dp3t.travel;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;
import java.util.Locale;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.UiUtils;

public class TravelUtils {

	public static String getCountryName(Context context, String countryCode) {
		Locale countryLocale = new Locale("", countryCode);
		Locale langLocale = new Locale(context.getString(R.string.language_key));
		return countryLocale.getDisplayCountry(langLocale);
	}

	public static void inflateFlagFlow(Flow flowConstraint, List<String> countries) {
		ConstraintLayout constraintLayout = (ConstraintLayout) flowConstraint.getParent();
		Context context = flowConstraint.getContext();
		LayoutInflater inflater = LayoutInflater.from(context);

		int[] flagViewIds = new int[countries.size()];
		for (int i = 0; i < countries.size(); i++) {
			String country = countries.get(i);
			if (TextUtils.isEmpty(country)) {
				continue;
			}

			View flagView = inflater.inflate(R.layout.view_country_flag, null);
			flagView.setId(View.generateViewId());
			flagView.setContentDescription(TravelUtils.getCountryName(context, country));

			ImageView flagImageView = flagView.findViewById(R.id.flag_icon);
			TextView flagTextView = flagView.findViewById(R.id.flag_cc);

			String idName = "flag_" + country.toLowerCase(Locale.GERMAN);
			int drawableRes = UiUtils.getDrawableResourceByName(context, idName);
			if (drawableRes != 0) {
				flagImageView.setImageResource(drawableRes);
				flagTextView.setVisibility(View.GONE);
			} else {
				flagImageView.setVisibility(View.GONE);
				flagTextView.setText(country);
			}

			flagViewIds[i] = flagView.getId();
			constraintLayout.addView(flagView);
		}
		flowConstraint.setReferencedIds(flagViewIds);
	}

}
