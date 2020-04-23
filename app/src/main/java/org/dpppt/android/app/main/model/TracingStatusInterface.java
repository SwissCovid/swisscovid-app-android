package org.dpppt.android.app.main.model;

import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.sdk.TracingStatus;

public interface TracingStatusInterface {

	boolean isReportedAsInfected();

	boolean wasContactReportedAsExposed();

	void setDebugAppState(DebugAppState debugAppState);

	DebugAppState getDebugAppState();

	@Deprecated
	AppState getAppState();

	TracingState getTracingState();

	NotificationState getNotificationState();

	TracingStatus.ErrorState getTracingErrorState();

}
