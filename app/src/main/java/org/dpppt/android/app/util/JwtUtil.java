package org.dpppt.android.app.util;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

import org.dpppt.android.app.inform.models.AccessTokenModel;

public class JwtUtil {

	private static final SimpleDateFormat ONSET_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static Date getOnsetDate(String accessToken) {
		String[] tokenParts = accessToken.split("\\.");
		if (tokenParts.length < 3) {
			return null;
		}
		String payloadString = new String(Base64.decode(tokenParts[1], Base64.NO_WRAP), StandardCharsets.UTF_8);
		AccessTokenModel tokenModel = new Gson().fromJson(payloadString, AccessTokenModel.class);
		if (tokenModel != null && tokenModel.getOnset() != null) {
			try {
				return ONSET_DATE_FORMAT.parse(tokenModel.getOnset());
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

}
