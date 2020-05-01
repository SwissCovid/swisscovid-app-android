/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.util;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;

import org.dpppt.android.app.inform.models.AccessTokenModel;

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
