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
import java.util.TimeZone;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.models.DayDate;
import org.dpppt.android.sdk.models.ExposureDay;

import ch.admin.bag.dp3t.debug.model.DebugAppState;
import ch.admin.bag.dp3t.main.model.NotificationState;
import ch.admin.bag.dp3t.main.model.TracingState;
import ch.admin.bag.dp3t.main.model.TracingStatusInterface;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.DateUtils;
import ch.admin.bag.dp3t.util.TracingErrorStateHelper;

public class TracingStatusWrapper implements TracingStatusInterface {

	private DebugAppState debugAppState = DebugAppState.NONE;
	private TracingStatus status;

	@Override
	public void setStatus(TracingStatus status) {
		this.status = status;
	}

	@Override
	public boolean isReportedAsInfected() {
		if (debugAppState == DebugAppState.NONE) {
			return status.getInfectionStatus() == InfectionStatus.INFECTED;
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
		}
		return status.getExposureDays();
	}

	@Override
	public boolean wasContactReportedAsExposed() {
		if (debugAppState == DebugAppState.NONE) {
			return status.getInfectionStatus() == InfectionStatus.EXPOSED;
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
			status = DP3T.getStatus(context);
			secureStorage.setReportsHeaderAnimationPending(false);
		} else {
			secureStorage.setReportsHeaderAnimationPending(false);
		}
	}

	public DebugAppState getDebugAppState() {
		return debugAppState;
	}

	@Override
	public TracingState getTracingState() {
		return status.isTracingEnabled() ? TracingState.ACTIVE : TracingState.NOT_ACTIVE;
	}

	@Override
	public TracingStatus.ErrorState getTracingErrorState() {
		boolean hasError = status.getErrors().size() > 0;
		if (hasError) {
			return TracingErrorStateHelper.getErrorState(status.getErrors());
		}
		return null;
	}

	@Override
	public TracingStatus.ErrorState getReportErrorState() {
		return TracingErrorStateHelper.getErrorStateForReports(status.getErrors());
	}

	@Override
	public long getDaysSinceExposure() {
		if (getExposureDays().size() > 0) {
			long time = getExposureDays().get(getExposureDays().size() - 1).getExposedDate().getStartOfDay(TimeZone.getDefault());
			return DateUtils.getDaysDiff(time);
		}
		return -1;
	}

	@Override
	public void resetInfectionStatus(Context context) {
		if (debugAppState == DebugAppState.REPORTED_EXPOSED) {
			debugAppState = DebugAppState.HEALTHY;
		} else {
			DP3T.resetInfectionStatus(context);
		}
	}

	@Override
	public boolean canInfectedStatusBeReset(Context context) {
		if (debugAppState == DebugAppState.REPORTED_EXPOSED) {
			return true;
		} else {
			return DP3T.getIAmInfectedIsResettable(context);
		}
	}

	@Override
	public void resetExposureDays(Context context) {
		debugAppState = DebugAppState.NONE;
		DP3T.resetExposureDays(context);
	}

	@Override
	public NotificationState getNotificationState() {
		switch (debugAppState) {
			case NONE:
				if (isReportedAsInfected()) {
					return NotificationState.POSITIVE_TESTED;
				} else if (wasContactReportedAsExposed()) {
					return NotificationState.EXPOSED;
				} else {
					return NotificationState.NO_REPORTS;
				}
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
