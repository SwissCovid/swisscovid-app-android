package ch.admin.bag.dp3t.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class FormatUtil {

	private static final double ONE_MILLION = 1000000;
	private static final String EMPTY_VALUE = "-";

	public static String formatNumberInMillions(Integer value) {
		if (value == null) {
			return EMPTY_VALUE;
		} else {
			DecimalFormat df = new DecimalFormat("#0.00");
			double valueInMillions = value / ONE_MILLION;
			return df.format(valueInMillions);
		}
	}

	public static String formatNumberInThousands(Integer value) {
		if (value == null) {
			return EMPTY_VALUE;
		} else {
			DecimalFormat df = new DecimalFormat("#,###");
			DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
			symbols.setGroupingSeparator(' ');
			df.setDecimalFormatSymbols(symbols);
			return df.format(value);
		}
	}

	public static String formatPercentage(Double value, int decimals, boolean alwaysShowSign) {
		if (value == null) {
			return EMPTY_VALUE;
		} else {
			String format;
			if (alwaysShowSign) {
				format = "%+." + decimals + "f";
			} else {
				format = "%." + decimals + "f";
			}

			return String.format(format, value * 100) + "%";
		}
	}

}
