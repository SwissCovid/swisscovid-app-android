/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.UserManager;

import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes;

public class ENExceptionHelper {

	private static final Pattern CONNECTION_RESULT_PATTERN =
			Pattern.compile("ConnectionResult\\{[^}]*statusCode=[a-zA-Z0-9_]+\\((\\d+)\\)");

	private static final String PACKAGE_RELEASE_SIGNATURE_SHA256 =
			"B2B9E6E6C6B323DF624CE5F62C9C1326871B8082C8CE0C0732A00C6984C60A6C";

	public static String getErrorMessage(Exception exception, Context context) {
		String errorDetailMessage = null;
		boolean attachExceptionMessage = true;
		if (exception instanceof ApiException) {
			Status status = ((ApiException) exception).getStatus();
			if (status.getStatusCode() == 17 && status.getStatusMessage() != null) {
				int connectionStatusCode = getConnectionStatusCode(status);
				switch (connectionStatusCode) {
					case ExposureNotificationStatusCodes.FAILED_NOT_SUPPORTED:
						if (!supportsBLE(context)) {
							errorDetailMessage = "Bluetooth Low Energy is not supported on this device.";
							attachExceptionMessage = false;
						} else if (!isUserDeviceOwner(context)) {
							errorDetailMessage = "ExposureNotifications are only supported for the main device user!";
							attachExceptionMessage = false;
						} else if (!supportsMultiAds()) {
							errorDetailMessage = "Bluetooth Multiple Advertisement is not supported on this device.";
						} else {
							errorDetailMessage = "This device does not support Exposure Notifications.";
						}
						break;
					case ExposureNotificationStatusCodes.FAILED_UNAUTHORIZED:
						if (!isPackageSignatureValid(context)) {
							errorDetailMessage = "Unauthorized package signature";
							attachExceptionMessage = false;
						} else {
							errorDetailMessage = "Unauthorized API usage.";
						}
						break;
					default:
						errorDetailMessage = ExposureNotificationStatusCodes.getStatusCodeString(connectionStatusCode);
				}
			}
		}
		if (errorDetailMessage != null) {
			if (attachExceptionMessage) {
				return errorDetailMessage + "\n\n" + exception.getMessage();
			} else {
				return errorDetailMessage;
			}
		} else {
			return exception.getMessage();
		}
	}

	private static int getConnectionStatusCode(Status status) {
		String statusMessage = status.getStatusMessage();
		if (statusMessage != null) {
			Matcher matcher = CONNECTION_RESULT_PATTERN.matcher(statusMessage);
			if (matcher.find()) {
				String connectionStatusCode = matcher.group(1);
				return Integer.parseInt(connectionStatusCode);
			}
		}
		return -2;
	}

	private static boolean supportsBLE(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}

	private static boolean supportsMultiAds() {
		return BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported();
	}

	private static boolean isPackageSignatureValid(Context context) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(signature.toByteArray());
				String signatureHash = StringUtil.toHex(md.digest()).toUpperCase();
				if (PACKAGE_RELEASE_SIGNATURE_SHA256.equals(signatureHash)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isUserDeviceOwner(Context context) {
		UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
		if (um != null) {
			return um.isSystemUser();
		}
		return true;
	}

}
