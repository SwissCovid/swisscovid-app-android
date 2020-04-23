/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.dpppt.android.app.R;

public enum TracingState {
	ACTIVE,
	NOT_ACTIVE;

	public static @StringRes
	int getTitle(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.string.tracing_active_title;
			case NOT_ACTIVE:
				return R.string.tracing_turned_off_title;
		}
		throw new IllegalStateException("Unknown State");
	}

	public static @StringRes
	int getText(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.string.tracing_active_text;
			case NOT_ACTIVE:
				return R.string.tracing_turned_off_text;
		}
		throw new IllegalStateException("Unknown State");
	}

	public static @DrawableRes
	int getIcon(TracingState tracingState) {
		switch (tracingState) {
			case ACTIVE:
				return R.drawable.ic_check;
			case NOT_ACTIVE:
		}
		throw new IllegalStateException("Unknown State");
	}
	}

