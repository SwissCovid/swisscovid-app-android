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

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;

import ch.admin.bag.dp3t.inform.models.AccessTokenModel;

public class JwtUtil {

	public static Date getOnsetDate(String accessToken) {
		String[] tokenParts = accessToken.split("\\.");
		if (tokenParts.length < 3) {
			return null;
		}
		String payloadString = new String(Base64.decode(tokenParts[1], Base64.NO_WRAP), StandardCharsets.UTF_8);
		AccessTokenModel tokenModel = new Gson().fromJson(payloadString, AccessTokenModel.class);
		if (tokenModel != null && tokenModel.getOnset() != null) {
			try {
				SimpleDateFormat onsetDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				onsetDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				return onsetDateFormat.parse(tokenModel.getOnset());
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

}
