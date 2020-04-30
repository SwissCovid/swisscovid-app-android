package org.dpppt.android.app.util;

import java.security.SecureRandom;

public class ExponentialDistribution {

	public static double sampleFromStandard() {
		SecureRandom random = new SecureRandom();
		return -Math.log(1.0d - random.nextDouble());
	}

}
