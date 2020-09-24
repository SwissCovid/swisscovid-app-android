/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.home.model;

import android.content.Context;

import java.util.List;
import java.util.TimeZone;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.models.ExposureDay;

import ch.admin.bag.dp3t.util.DateUtils;
import ch.admin.bag.dp3t.util.TracingErrorStateHelper;

public abstract class DefaultTracingStatusWrapper implements TracingStatusInterface {

	private TracingStatus status;

	@Override
	public void setStatus(TracingStatus status) {
		this.status = status;
	}

	@Override
	public boolean isReportedAsInfected() {
		return status.getInfectionStatus() == InfectionStatus.INFECTED;
	}

	@Override
	public List<ExposureDay> getExposureDays() {
		return status.getExposureDays();
	}

	@Override
	public boolean wasContactReportedAsExposed() {
		return status.getInfectionStatus() == InfectionStatus.EXPOSED;
	}

	@Override
	public TracingState getTracingState() {
		return status.isTracingEnabled() ? TracingState.ACTIVE : TracingState.NOT_ACTIVE;
	}

	@Override
	public NotificationState getNotificationState() {
		if (isReportedAsInfected()) {
			return NotificationState.POSITIVE_TESTED;
		} else if (wasContactReportedAsExposed()) {
			return NotificationState.EXPOSED;
		} else {
			return NotificationState.NO_REPORTS;
		}
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
	public long getDaysSinceExposure() {
		if (getExposureDays().size() > 0) {
			long time = getExposureDays().get(getExposureDays().size() - 1).getExposedDate().getStartOfDay(TimeZone.getDefault());
			return DateUtils.getDaysDiff(time);
		}
		return -1;
	}

	@Override
	public void resetInfectionStatus(Context context) {
		DP3T.resetInfectionStatus(context);
	}

	@Override
	public boolean canInfectedStatusBeReset(Context context) {
		return DP3T.getIAmInfectedIsResettable(context);
	}

	@Override
	public void resetExposureDays(Context context) {
		DP3T.resetExposureDays(context);
	}

	@Override
	public TracingStatus.ErrorState getReportErrorState() {
		return TracingErrorStateHelper.getErrorStateForReports(status.getErrors());
	}

}
