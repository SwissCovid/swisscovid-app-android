/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.inform;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.google.gson.Gson;

import org.dpppt.android.app.R;
import org.dpppt.android.app.inform.model.AccessTokenModel;
import org.dpppt.android.app.inform.views.ChainedEditText;
import org.dpppt.android.app.util.InfoDialog;
import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.backend.CallbackListener;
import org.dpppt.android.sdk.internal.backend.ResponseException;
import org.dpppt.android.sdk.internal.backend.models.ExposeeAuthData;

public class InformFragment extends Fragment {

	private static final String PREFS_INFORM = "PREFERENCES_INFORM";
	private static final String PREFS_INFORM_TIME_REQ = "PREFS_INFORM_TIME_REQ";
	private static final String PREFS_INFORM_CODE_REQ = "PREFS_INFORM_CODE_REQ";
	private static final String PREFS_INFORM_TOKEN_REQ = "PREFS_INFORM_TOKEN_REQ";

	private static final long TIMEOUT_VALID_CODE = 1000 * 60 * 5;

	private static final String REGEX_CODE_PATTERN = "\\d{12}";

	private ChainedEditText authCodeInput;
	private AlertDialog progressDialog;

	public static InformFragment newInstance() {
		return new InformFragment();
	}

	public InformFragment() {
		super(R.layout.fragment_inform);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Button buttonSend = view.findViewById(R.id.trigger_fragment_button_trigger);
		authCodeInput = view.findViewById(R.id.trigger_fragment_input);
		authCodeInput.addTextChangedListener(new ChainedEditText.ChainedEditTextListener() {
			@Override
			public void onTextChanged(String input) {
				buttonSend.setEnabled(input.matches(REGEX_CODE_PATTERN));
			}

			@Override
			public void onEditorSendAction() {
				if (buttonSend.isEnabled()) buttonSend.callOnClick();
			}
		});

		SharedPreferences preferences = getContext().getSharedPreferences(PREFS_INFORM, Context.MODE_PRIVATE);
		long lastRequest = preferences.getLong(PREFS_INFORM_TIME_REQ, 0);
		String lastCode = preferences.getString(PREFS_INFORM_CODE_REQ, null);
		String lastToken = preferences.getString(PREFS_INFORM_TOKEN_REQ, null);

		if (System.currentTimeMillis() - lastRequest < TIMEOUT_VALID_CODE) {
			authCodeInput.setText(new String(Base64.decode(lastCode, Base64.NO_WRAP), StandardCharsets.UTF_8));
		}

		buttonSend.setOnClickListener(v -> {
			setInvalidCodeErrorVisible(false);
			String authCodeBase64 =
					new String(Base64.encode(authCodeInput.getText().getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP),
							StandardCharsets.UTF_8);

			progressDialog = createProgressDialog();
			if (System.currentTimeMillis() - lastRequest < TIMEOUT_VALID_CODE && lastToken != null) {
				String onsetDate = getOnsetDate(lastToken);
				informExposed(onsetDate, lastToken);
			} else {
				authenticateInput(authCodeBase64);
			}
		});

		view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
			getActivity().onBackPressed();
		});

		view.findViewById(R.id.trigger_fragment_no_code_button).setOnClickListener(v -> {
			getParentFragmentManager().beginTransaction()
					.replace(R.id.inform_fragment_container, NoCodeFragment.newInstance())
					.addToBackStack(NoCodeFragment.class.getCanonicalName())
					.commit();
		});
	}

	private void authenticateInput(String authCodeBase64) {
		// TODO: Request
		String accessToken =
				"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1LVJxcVRUWW9tZnBWejA2VlJFT2ZyYmNxYTVXdTJkX1g4MnZfWlRNLVZVIn0" +
						".eyJqdGkiOiJkODA1ZDFiNi1jZDU3LTQ0YzUtYjA3ZS0zZDFkMjgxOGNhZWQiLCJleHAiOjE1ODc2NDI3MzUsIm5iZiI6MCwiaWF0IjoxNTg3NjQyNDM1LCJpc3MiOiJodHRwczovL2lkZW50aXR5LXIuYml0LmFkbWluLmNoL3JlYWxtcy9iYWctcHRzIiwic3ViIjoiNWE1NDAwNDItZWI4ZS00ZWZiLWIyYmItZTc0ZjMxNjFkNjRiIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicHRhLWFwcC1iYWNrZW5kIiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiMmVkMDQ5OTktMzMzMi00Y2MyLWI2NDUtNzEzZjk3YzIwZjVlIiwiYWNyIjoiMSIsInNjb3BlIjoiZXhwb3NlZCIsIm9uc2V0IjoiMjAyMC0wNC0yMiIsInV1aWQiOiIrIDJmZTYzYjMyLTNjNzktNGY4Zi1iZGUyLWI1NDFlMDM3MjU5NSArICJ9.Qdo06_lZhtKVBdlfSOb7FhkpuQE4eC7ob9Nsj8B-GjXo_TgG6W7Rbq89xkTUZpsqnB56q4IK13Y5SE5TyDVoTctNmxO3QlL2MWMM7Uwge2U9MbPoS8hJf71RjEadWpvL2AvXEruwv7R3PzQ8hHObYFsSkWP4JZKBkp7vNnUjE1IpcyURNwgHObaonUbMog9F4QwPE0uVng8XwamNNCwL8n9ctoDGrXQESepeqf-Qg-6IbZ6LMF47dh2DOcDpUY0SRlv6nntphidoo0rDxRxUdCNYLORG9xpaRy5Oiqg_fLG32UzcgMK_YSfNWSo-UDq4uYC1prdb3DamKhTkvjkIAQ";

		SharedPreferences prefs = getContext().getSharedPreferences(PREFS_INFORM, Context.MODE_PRIVATE);
		prefs.edit()
				.putLong(PREFS_INFORM_TIME_REQ, System.currentTimeMillis())
				.putString(PREFS_INFORM_CODE_REQ, authCodeBase64)
				.putString(PREFS_INFORM_TOKEN_REQ, accessToken)
				.commit();

		String onsetDate = getOnsetDate(accessToken);
		if (onsetDate == null)
			showErrorDialog(getString(R.string.unexpected_error_title).replace("{ERROR}", "Received unreadable jwt access-token" +
					"."));
		informExposed(onsetDate, accessToken);
	}

	private void informExposed(String onsetDate, String accessToken) {
		DP3T.sendIAmInfected(getContext(), new Date()/*onsetDate*/,
				new ExposeeAuthData(accessToken), null, new CallbackListener<Void>() {
					@Override
					public void onSuccess(Void response) {
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						getContext().getSharedPreferences(PREFS_INFORM, Context.MODE_PRIVATE).edit().clear().commit();
						getParentFragmentManager().beginTransaction()
								.replace(R.id.inform_fragment_container, ThankYouFragment.newInstance())
								.commit();
					}

					@Override
					public void onError(Throwable throwable) {
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						String error;
						error = getString(R.string.unexpected_error_title).replace("{ERROR}",
								throwable instanceof ResponseException ? throwable.getMessage() :
								throwable instanceof IOException ? throwable.getLocalizedMessage() : "");
						showErrorDialog(error);
						throwable.printStackTrace();
					}
				});
	}

	private String getOnsetDate(String accessToken) {
		String[] tokenParts = accessToken.split("\\.");
		if (tokenParts.length < 3) {
			return null;
		}
		String payloadString = new String(Base64.decode(tokenParts[1], Base64.NO_WRAP), StandardCharsets.UTF_8);
		AccessTokenModel tokenModel = new Gson().fromJson(payloadString, AccessTokenModel.class);
		if (tokenModel != null && tokenModel.getOnset() != null) {
			return tokenModel.getOnset();
		}
		return null;
	}

	@Override
	public void onResume() {
		super.onResume();
		authCodeInput.requestFocus();
	}

	private void setInvalidCodeErrorVisible(boolean visible) {
		getView().findViewById(R.id.inform_invalid_code_error).setVisibility(visible ? View.VISIBLE : View.GONE);
		getView().findViewById(R.id.inform_input_text).setVisibility(visible ? View.GONE : View.VISIBLE);
	}

	private AlertDialog createProgressDialog() {
		return new AlertDialog.Builder(getContext())
				.setView(R.layout.dialog_loading)
				.show();
	}

	private void showErrorDialog(String error) {
		InfoDialog.newInstance(error)
				.show(getChildFragmentManager(), InfoDialog.class.getCanonicalName());
	}

}
