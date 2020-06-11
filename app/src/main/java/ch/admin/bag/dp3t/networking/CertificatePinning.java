/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.networking;

import okhttp3.CertificatePinner;

public class CertificatePinning {

	private static final CertificatePinner CERTIFICATE_PINNER_LIVE = new CertificatePinner.Builder()
			.add("www.pt-d.bfs.admin.ch", "sha256/xWdkLqfT40GnyHyZXt9IStltvrshlowMuGZHgp631Tw=") // leaf
			.add("www.pt1-d.bfs.admin.ch", "sha256/Pr8nx8M3Oa8EYefVXYB3D4KJViREDy4ipA1oVyjGoss=") // leaf
			.add("codegen-service-d.bag.admin.ch", "sha256/GLV5pALJpJb02GavguT3NTOmOL57H7K3KhJ59iH6A/Q=") //leaf
			.add("www.pt-t.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("www.pt1-t.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("codegen-service-t.bag.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("www.pt-a.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("www.pt1-a.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("codegen-service-a.bag.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("www.pt.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("www.pt1.bfs.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.add("codegen-service.bag.admin.ch", "sha256/KM3iZPSceB+hgYuNI+cSg4LRgTiUxCeGjrfXRQAY6Rs=") // intermediate
			.build();

	private static final CertificatePinner CERTIFICATE_PINNER_DISABLED = new CertificatePinner.Builder().build();

	private static boolean isEnabled = true;

	public static CertificatePinner getCertificatePinner() {
		return isEnabled ? CERTIFICATE_PINNER_LIVE : CERTIFICATE_PINNER_DISABLED;
	}

	public static boolean isEnabled() {
		return isEnabled;
	}

	public static void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}

}
