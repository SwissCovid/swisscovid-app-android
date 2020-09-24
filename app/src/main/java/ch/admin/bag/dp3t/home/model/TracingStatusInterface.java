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

import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.models.ExposureDay;

public interface TracingStatusInterface {

	void setStatus(TracingStatus status);

	boolean isReportedAsInfected();

	List<ExposureDay> getExposureDays();

	boolean wasContactReportedAsExposed();

	TracingState getTracingState();

	NotificationState getNotificationState();

	TracingStatus.ErrorState getTracingErrorState();

	TracingStatus.ErrorState getReportErrorState();

	long getDaysSinceExposure();

	void resetInfectionStatus(Context context);

	void resetExposureDays(Context context);

	boolean canInfectedStatusBeReset(Context context);

}
