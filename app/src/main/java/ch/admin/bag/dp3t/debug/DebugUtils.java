/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.debug;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;

import ch.admin.bag.dp3t.R;

class DebugUtils {

	private static final DateFormat DATE_FORMAT_SYNC = SimpleDateFormat.getDateTimeInstance();

	protected static SpannableString formatStatusString(TracingStatus status, Context context) {
		SpannableStringBuilder builder = new SpannableStringBuilder();
		boolean isTracing = status.isTracingEnabled();
		builder.append(context.getString(isTracing ? R.string.tracing_active_title : R.string.android_tracing_error_title)).append("\n")
				.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		long lastSyncDateUTC = status.getLastSyncDate();
		String lastSyncDateString =
				lastSyncDateUTC > 0 ? DATE_FORMAT_SYNC.format(new Date(lastSyncDateUTC)) : "n/a";
		builder.append(context.getString(R.string.debug_sdk_state_last_synced))
				.append(lastSyncDateString).append("\n")
				.append(context.getString(R.string.debug_sdk_state_self_exposed))
				.append(getBooleanDebugString(status.getInfectionStatus() == InfectionStatus.INFECTED,context)).append("\n")
				.append(context.getString(R.string.debug_sdk_state_contact_exposed))
				.append(getBooleanDebugString(status.getInfectionStatus() == InfectionStatus.EXPOSED, context));

		Collection<TracingStatus.ErrorState> errors = status.getErrors();
		if (errors != null && errors.size() > 0) {
			int start = builder.length();
			builder.append("\n");
			for (TracingStatus.ErrorState error : errors) {
				builder.append("\n").append(error.toString());
			}
			builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.red_main)),
					start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return new SpannableString(builder);
	}

	private static String getBooleanDebugString(boolean value, Context context) {
		return context.getString(value ? R.string.debug_sdk_state_boolean_true : R.string.debug_sdk_state_boolean_false);
	}

}
