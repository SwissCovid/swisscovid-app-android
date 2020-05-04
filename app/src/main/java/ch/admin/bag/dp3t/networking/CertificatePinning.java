/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package ch.admin.bag.dp3t.networking;

import okhttp3.CertificatePinner;

public class CertificatePinning {

	private static CertificatePinner instance;

	public static CertificatePinner getCertificatePinner() {
		if (instance == null) {
			instance = new CertificatePinner.Builder()
					.add("www.pt-d.bfs.admin.ch", "sha256/xWdkLqfT40GnyHyZXt9IStltvrshlowMuGZHgp631Tw=") // leaf
					.add("www.pt1-d.bfs.admin.ch", "sha256/Pr8nx8M3Oa8EYefVXYB3D4KJViREDy4ipA1oVyjGoss=") // leaf
					.add("www.pt-a.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
					.add("www.pt1-a.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
					.add("www.pt.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
					.add("www.pt1.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
					.build();
		}
		return instance;
	}

}
