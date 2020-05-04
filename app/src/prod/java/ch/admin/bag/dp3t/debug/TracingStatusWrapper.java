/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.debug;

import java.util.List;
import java.util.TimeZone;

import ch.admin.bag.dp3t.main.model.NotificationState;
import ch.admin.bag.dp3t.main.model.TracingState;
import ch.admin.bag.dp3t.main.model.TracingStatusInterface;
import ch.admin.bag.dp3t.util.DateUtils;
import ch.admin.bag.dp3t.util.TracingErrorStateHelper;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.database.models.ExposureDay;

public class TracingStatusWrapper implements TracingStatusInterface {

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
		boolean tracingOff = !(status.isAdvertising() || status.isReceiving());
		return tracingOff ? TracingState.NOT_ACTIVE : TracingState.ACTIVE;
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
			long time = getExposureDays().get(0).getExposedDate().getStartOfDay(TimeZone.getDefault());
			return DateUtils.getDaysDiff(time);
		}
		return -1;
	}


	@Override
	public TracingStatus.ErrorState getReportErrorState() {
		boolean hasError = status.getErrors().size() > 0;
		if (hasError) {
			TracingStatus.ErrorState errorState = TracingErrorStateHelper.getErrorStateForReports(status.getErrors());
			if (TracingStatus.ErrorState.SYNC_ERROR_DATABASE.equals(errorState)) {
				return errorState;
			} else {
				if (DateUtils.getDaysDiff(status.getLastSyncDate()) > 1) {
					return errorState;
				}
			}
		}
		return null;
	}

}
