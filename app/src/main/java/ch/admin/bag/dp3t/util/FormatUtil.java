package ch.admin.bag.dp3t.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FormatUtil {

	private static final double ONE_MILLION = 1000000;

	public static String formatNumberInMillions(int value) {
		DecimalFormat df = new DecimalFormat("##.##");
		double valueInMillions = value / ONE_MILLION;
		return df.format(valueInMillions);
	}

	public static String formatNumberInThousands(int value) {
		DecimalFormat df = new DecimalFormat("#,###");
		DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		df.setDecimalFormatSymbols(symbols);
		return df.format(value);
	}

	public static String formatPercentage(double value, int decimals) {
		return String.format("%." + decimals + "f", value * 100) + "%";
	}

}
