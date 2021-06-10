package ch.admin.bag.dp3t.checkin.networking;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.crowdnotifier.android.sdk.CrowdNotifier;
import org.crowdnotifier.android.sdk.model.ExposureEvent;
import org.crowdnotifier.android.sdk.model.ProblematicEventInfo;
import org.dpppt.android.sdk.DP3T;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.NotificationUtil;

import static ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper.autoCheckoutIfNecessary;
import static org.dpppt.android.sdk.InfectionStatus.INFECTED;


public class CrowdNotifierKeyLoadWorker extends Worker {

	public static final String ACTION_NEW_TRACE_KEY_SYNC = BuildConfig.APPLICATION_ID + ".ACTION_NEW_TRACE_KEY_SYNC";
	private static final String WORK_TAG = "ch.admin.bag.dp3t.checkin.networking.CrowdNotifierKeyLoadWorker";
	private static final int DAYS_TO_KEEP_VENUE_VISITS = 14;
	private static final int REPEAT_INTERVAL_MINUTES = 120;
	private static final String LOG_TAG = "KeyLoadWorker";

	public static void startKeyLoadWorker(Context context) {
		Constraints constraints = new Constraints.Builder()
				.setRequiredNetworkType(NetworkType.CONNECTED)
				.build();

		PeriodicWorkRequest periodicWorkRequest =
				new PeriodicWorkRequest.Builder(CrowdNotifierKeyLoadWorker.class, REPEAT_INTERVAL_MINUTES, TimeUnit.MINUTES)
						.setConstraints(constraints)
						.build();

		WorkManager workManager = WorkManager.getInstance(context);
		workManager.enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);
	}


	public CrowdNotifierKeyLoadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		Log.d(LOG_TAG, "Started KeyLoadWorker");
		if (DP3T.getStatus(getApplicationContext()).getInfectionStatus() == INFECTED) {
			Log.d(LOG_TAG, "KeyLoadWorker: Network Request not executed");
			return Result.success();
		}

		List<ProblematicEventInfo> problematicEventInfos = new TraceKeysRepository(getApplicationContext()).loadTraceKeys();
		if (problematicEventInfos == null) {
			Log.d(LOG_TAG, "KeyLoadWorker failure");
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_NEW_TRACE_KEY_SYNC));
			return Result.retry();
		}
		List<ExposureEvent> exposures =
				CrowdNotifier.checkForMatches(problematicEventInfos, getApplicationContext());
		if (!exposures.isEmpty()) {
			showExposureNotification();
		}
		cleanUpOldData(getApplicationContext());
		autoCheckoutIfNecessary(getApplicationContext(), SecureStorage.getInstance(getApplicationContext()).getCheckInState());

		SecureStorage.getInstance(getApplicationContext()).setLastSuccessfulCheckinDownload(System.currentTimeMillis());
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_NEW_TRACE_KEY_SYNC));
		Log.d(LOG_TAG, "KeyLoadWorker success");
		return Result.success();
	}

	private void showExposureNotification() {
		SecureStorage secureStorage = SecureStorage.getInstance(getApplicationContext());
		NotificationUtil.generateContactNotification(getApplicationContext());
		secureStorage.setAppOpenAfterNotificationPending(true);
		secureStorage.setReportsHeaderAnimationPending(true);
	}

	public static void cleanUpOldData(Context context) {
		CrowdNotifier.cleanUpOldData(context, DAYS_TO_KEEP_VENUE_VISITS);
		DiaryStorage.getInstance(context).removeEntriesBefore(DAYS_TO_KEEP_VENUE_VISITS);
	}

}
