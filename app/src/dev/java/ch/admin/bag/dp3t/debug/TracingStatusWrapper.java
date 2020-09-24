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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.models.DayDate;
import org.dpppt.android.sdk.models.ExposureDay;

import ch.admin.bag.dp3t.debug.model.DebugAppState;
import ch.admin.bag.dp3t.home.model.DefaultTracingStatusWrapper;
import ch.admin.bag.dp3t.home.model.NotificationState;
import ch.admin.bag.dp3t.storage.SecureStorage;

public class TracingStatusWrapper extends DefaultTracingStatusWrapper {

	private DebugAppState debugAppState = DebugAppState.NONE;

	@Override
	public boolean isReportedAsInfected() {
		if (debugAppState == DebugAppState.NONE) {
			return super.isReportedAsInfected();
		} else {
			return debugAppState == DebugAppState.REPORTED_EXPOSED;
		}
	}

	@Override
	public List<ExposureDay> getExposureDays() {
		if (debugAppState == DebugAppState.CONTACT_EXPOSED) {
			List<ExposureDay> matches = new ArrayList<>();
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, -3);
			matches.add(new ExposureDay(0, new DayDate(calendar.getTimeInMillis()), System.currentTimeMillis()));
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			matches.add(new ExposureDay(1, new DayDate(calendar.getTimeInMillis()), System.currentTimeMillis()));
			return matches;
		} else {
			return super.getExposureDays();
		}
	}

	@Override
	public boolean wasContactReportedAsExposed() {
		if (debugAppState == DebugAppState.NONE) {
			return super.wasContactReportedAsExposed();
		} else {
			return debugAppState == DebugAppState.CONTACT_EXPOSED;
		}
	}

	public void setDebugAppState(Context context, DebugAppState debugAppState) {
		this.debugAppState = debugAppState;
		SecureStorage secureStorage = SecureStorage.getInstance(context);
		if (debugAppState == DebugAppState.CONTACT_EXPOSED) {
			secureStorage.setReportsHeaderAnimationPending(true);
		} else if (debugAppState == DebugAppState.REPORTED_EXPOSED) {
			DP3T.stop(context);
			setStatus(DP3T.getStatus(context));
			secureStorage.setReportsHeaderAnimationPending(false);
		} else {
			secureStorage.setReportsHeaderAnimationPending(false);
		}
	}

	public DebugAppState getDebugAppState() {
		return debugAppState;
	}

	@Override
	public void resetInfectionStatus(Context context) {
		if (debugAppState == DebugAppState.REPORTED_EXPOSED) {
			debugAppState = DebugAppState.HEALTHY;
		} else {
			super.resetInfectionStatus(context);
		}
	}

	@Override
	public boolean canInfectedStatusBeReset(Context context) {
		if (debugAppState == DebugAppState.REPORTED_EXPOSED) {
			return true;
		} else {
			return super.canInfectedStatusBeReset(context);
		}
	}

	@Override
	public void resetExposureDays(Context context) {
		if (debugAppState == DebugAppState.CONTACT_EXPOSED) {
			debugAppState = DebugAppState.NONE;
		} else {
			super.resetExposureDays(context);
		}
	}

	@Override
	public NotificationState getNotificationState() {
		switch (debugAppState) {
			case NONE:
				return super.getNotificationState();
			case HEALTHY:
				return NotificationState.NO_REPORTS;
			case REPORTED_EXPOSED:
				return NotificationState.POSITIVE_TESTED;
			case CONTACT_EXPOSED:
				return NotificationState.EXPOSED;
		}
		throw new IllegalStateException("Unknown debug AppState: " + debugAppState.toString());
	}

}
