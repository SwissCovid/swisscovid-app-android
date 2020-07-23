package ch.admin.bag.dp3t.util;

import java.security.SecureRandom;

public class PoissonDistribution {

	public static int sample(double lambda) {
		double L = Math.exp(-lambda);
		double p = 1.0;
		int k = 0;

		do {
			k++;
			p *= Math.random();
		} while (p > L);
		return k - 1;
	}

}
