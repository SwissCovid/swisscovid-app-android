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


import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.dpppt.android.sdk.models.DayDate;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class VerificationCodeTests {

	@Test
	public void testGeneration() throws ParseException, UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeyException {
		String tweak = "thisisthetweakthatweuseforthesetestshereonly";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zurich"));

		DayDate exposureDate = new DayDate(sdf.parse("20200612-18:37:00").getTime());
		assertEquals("320303", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:50:00").getTime()));
		assertEquals("320303", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:52:12").getTime()));
		assertEquals("320303", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:54:59").getTime()));
		assertEquals("922616", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:55:00").getTime()));

		exposureDate = new DayDate(sdf.parse("20200610-02:12:00").getTime());
		assertEquals("499968", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:50:00").getTime()));
		assertEquals("499968", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:52:12").getTime()));
		assertEquals("499968", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:54:59").getTime()));
		assertEquals("499746", VerificationCode.generateCode(exposureDate, tweak, sdf.parse("20200614-18:55:00").getTime()));
	}

}
