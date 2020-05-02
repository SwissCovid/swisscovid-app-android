/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.util;

import java.security.SecureRandom;

public class ExponentialDistribution {

	public static double sampleFromStandard() {
		SecureRandom random = new SecureRandom();
		return -Math.log(1.0d - random.nextDouble());
	}

}
