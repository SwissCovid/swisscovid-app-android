package org.dpppt.android.app.debug;

import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.app.main.model.NotificationState;
import org.dpppt.android.app.main.model.TracingState;
import org.dpppt.android.app.main.model.TracingStatusInterface;
import org.dpppt.android.app.util.TracingErrorStateHelper;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;

public class TracingStatusWrapper implements TracingStatusInterface {

	private DebugAppState debugAppState = DebugAppState.NONE;
	private TracingStatus status;

	public TracingStatusWrapper(DebugAppState debugAppState) {
		this.debugAppState = debugAppState;
	}

	public void setStatus(TracingStatus status) {
		this.status = status;
	}

	@Override
	public boolean isReportedAsInfected() {
		return status.getInfectionStatus() == InfectionStatus.INFECTED || debugAppState == DebugAppState.REPORTED_EXPOSED;
	}

	@Override
	public boolean wasContactReportedAsExposed() {
		return status.getInfectionStatus() == InfectionStatus.EXPOSED || debugAppState == DebugAppState.CONTACT_EXPOSED;
	}

	@Override
	public void setDebugAppState(DebugAppState debugAppState) {
		this.debugAppState = debugAppState;
	}

	@Override
	public DebugAppState getDebugAppState() {
		return debugAppState;
	}

	@Override
	public TracingState getTracingState() {
		boolean tracingOff = !(status.isAdvertising() || status.isReceiving());
		return tracingOff ? TracingState.NOT_ACTIVE : TracingState.ACTIVE;
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
		throw new IllegalStateException("Unkown debug AppState: " + debugAppState.toString());
	}

}
