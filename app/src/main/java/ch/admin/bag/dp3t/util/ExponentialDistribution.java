/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package ch.admin.bag.dp3t.util;

import java.security.SecureRandom;

public class ExponentialDistribution {

	public static double sampleFromStandard() {
		SecureRandom random = new SecureRandom();
		return -Math.log(1.0d - random.nextDouble());
	}

}
