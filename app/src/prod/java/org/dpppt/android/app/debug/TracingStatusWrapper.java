package org.dpppt.android.app.debug;

import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.app.main.model.NotificationState;
import org.dpppt.android.app.main.model.TracingState;
import org.dpppt.android.app.main.model.TracingStatusInterface;
import org.dpppt.android.app.util.TracingErrorStateHelper;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.database.models.MatchedContact;

public class TracingStatusWrapper implements TracingStatusInterface {

	private DebugAppState debugAppState = DebugAppState.NONE;
	private TracingStatus status;

	public TracingStatusWrapper(DebugAppState debugAppState) {
		//Always none on PROD
		this.debugAppState = DebugAppState.NONE; ;
	}

	public void setStatus(TracingStatus status) {
		this.status = status;
	}

	@Override
	public boolean isReportedAsInfected() {
		return status.getInfectionStatus() == InfectionStatus.INFECTED;
	}

	@Override
	public long getDaySinceExposed() {
		long time = 0;
		for (MatchedContact matchedContact : status.getMatchedContacts()) {
			if (time < matchedContact.getReportDate()) {
				time = matchedContact.getReportDate();
			}
		}
		return DateUtils.getDaysDiff(time);
	}

	@Override
	public boolean wasContactReportedAsExposed() {
		return status.getInfectionStatus() == InfectionStatus.EXPOSED;
	}

	@Override
	public void setDebugAppState(DebugAppState debugAppState) {
		//do not implement
	}

	@Override
	public DebugAppState getDebugAppState() {
		return DebugAppState.NONE;
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
	public TracingStatus.ErrorState getReportErrorState() {
		boolean hasError = status.getErrors().size() > 0;
		if (hasError) {
			return TracingErrorStateHelper.getErrorStateForReports(status.getErrors());
		}
		return null;
	}

}
