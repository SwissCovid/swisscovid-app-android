/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.reports;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.dpppt.android.sdk.models.DayDate;

public class VerificationCode {

	public static String generateCode(DayDate exposure, String tweak)
			throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
		return generateCode(exposure, tweak, System.currentTimeMillis());
	}

	protected static String generateCode(DayDate exposure, String tweak, long currentTime)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
		ByteBuffer info = ByteBuffer.allocate(16);
		info.putLong(exposure.getStartOfDayTimestamp());
		info.putLong(currentTime - currentTime % (5 * 60 * 1000l));
		return TRUNCATE(HKDF.HKDF(tweak.getBytes("UTF-8"), null, info.array(), 4));
	}

	private static String TRUNCATE(byte[] data) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.put(data);
		buffer.position(0);
		int number = buffer.getInt();
		String formated = String.format("%06d", number);
		return formated.substring(formated.length() - 6);
	}

	public static class HKDF {

		private static final int hash_len = 32;

		private static byte[] hmac_sha256(byte[] data, byte[] ikm) throws NoSuchAlgorithmException, InvalidKeyException {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(ikm, "HmacSHA256");
			sha256_HMAC.init(secret_key);
			return sha256_HMAC.doFinal(data);
		}

		public static byte[] HKDF(byte[] key, byte[] salt, byte[] info, int length)
				throws InvalidKeyException, NoSuchAlgorithmException {

			if (salt == null || salt.length == 0) {
				salt = new byte[hash_len];
			}

			byte[] prk = hmac_sha256(key, salt);
			byte[] t = new byte[0];
			ByteBuffer okm = ByteBuffer.allocate((int) (Math.ceil(length * 1.0 / hash_len) * hash_len));
			for (byte i = 0; i < Math.ceil(length * 1.0 / hash_len); i++) {
				t = hmac_sha256(merge(t, info, (byte) (1 + i)), prk);
				okm.put(t);
			}
			byte[] result = new byte[length];
			okm.position(0);
			okm.get(result, 0, length);
			return result;
		}

		private static byte[] merge(byte[] data, byte[] data2, byte lastByte) {
			ByteBuffer result = ByteBuffer.allocate(data.length + data2.length + 1);
			result.put(data);
			result.put(data2);
			result.put(lastByte);
			return result.array();
		}

	}

}
