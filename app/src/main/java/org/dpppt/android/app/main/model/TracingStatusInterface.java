package org.dpppt.android.app.main.model;

import java.util.List;

import org.dpppt.android.app.debug.model.DebugAppState;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.database.models.ExposureDay;

public interface TracingStatusInterface {

	boolean isReportedAsInfected();

	List<ExposureDay> getExposureDays();

	boolean wasContactReportedAsExposed();

	void setDebugAppState(DebugAppState debugAppState);

	DebugAppState getDebugAppState();

	TracingState getTracingState();

	NotificationState getNotificationState();

	TracingStatus.ErrorState getTracingErrorState();

	TracingStatus.ErrorState getReportErrorState();

	long getDaysSinceExposure();

}
