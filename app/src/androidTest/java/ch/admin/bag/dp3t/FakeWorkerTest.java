package ch.admin.bag.dp3t;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.work.Configuration;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.TestDriver;
import androidx.work.testing.WorkManagerTestInitHelper;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import junit.framework.TestCase;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.LogLevel;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.models.ApplicationInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.admin.bag.dp3t.networking.FakeWorker;
import ch.admin.bag.dp3t.storage.SecureStorage;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FakeWorkerTest {

	Context context;
	MockWebServer server;
	TestDriver testDriver;

	@Before
	public void setup() throws IOException {
		context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Logger.init(context, LogLevel.DEBUG);

		//cancel all work that was scheduled on normal WorkManager on Application creation
		WorkManager.getInstance(context).cancelAllWork();

		// Initialize WorkManager for instrumentation tests.
		Configuration config = new Configuration.Builder()
				// Set log level to Log.DEBUG to make it easier to debug
				.setMinimumLoggingLevel(Log.DEBUG)
				.setExecutor(new SynchronousExecutor())
				.build();
		WorkManagerTestInitHelper.initializeTestWorkManager(context, config);

		testDriver = WorkManagerTestInitHelper.getTestDriver(context);

		server = new MockWebServer();
		server.start();

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		DP3T.init(context, new ApplicationInfo(server.url("/bucket/").toString(), server.url("/report/").toString()),
				null);
		appConfigManager.setTracingEnabled(false);
		DP3T.clearData(context);
		DP3T.init(context, new ApplicationInfo(server.url("/bucket/").toString(), server.url("/report/").toString()),
				null);
		appConfigManager.setTracingEnabled(true);

		SecureStorage.getInstance(context).setTDummy(-1);

		//cancel all work that was scheduled during initialization
		WorkManager.getInstance(context).cancelAllWork();
	}

	@Test
	public void testInitialTDummy() throws Exception {
		List<WorkInfo> initialWorkList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(0, initialWorkList.size());

		FakeWorker.safeStartFakeWorker(context);
		// TDummy is initialized to a time in the future.
		assertTrue(SecureStorage.getInstance(context).getTDummy() > System.currentTimeMillis());
	}

	private void assertIsConnected() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Network net = cm.getActiveNetwork();
		TestCase.assertNotNull("A network connection is required for this test", net);
	}

	@Test
	public void testCallingReportWhenScheduledIsNotPast() throws Exception {
		assertIsConnected();

		List<WorkInfo> initialWorkList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(0, initialWorkList.size());

		long t_dummy = setTDummyToDaysFromNow(1);
		AtomicInteger requestCounter = new AtomicInteger(0);

		server.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				requestCounter.getAndIncrement();
				return new MockResponse().setResponseCode(200);
			}
		});

		FakeWorker.safeStartFakeWorker(context);
		WorkInfo workInfo = executeWorker();
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();

		// Worker succeeds by not executing a request. TDummy stays the same.
		assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
		assertEquals(0, requestCounter.get());
		assertEquals(t_dummy, new_t_dummy);
	}

	@Test
	public void testCallingReportWhenScheduledIsPast() throws Exception {
		assertIsConnected();

		List<WorkInfo> initialWorkList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(0, initialWorkList.size());

		long t_dummy = setTDummyToDaysFromNow(-1);
		AtomicInteger requestCounter = new AtomicInteger(0);

		server.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				requestCounter.getAndIncrement();
				return new MockResponse().setResponseCode(200);
			}
		});

		FakeWorker.safeStartFakeWorker(context);
		WorkInfo workInfo = executeWorker();
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();

		// Worker succeeds by executing at least one request. The new_t_dummy must be greater than the old t_dummy.
		assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
		assertTrue(new_t_dummy > t_dummy);
		assertTrue(requestCounter.get() >= 1);
	}

	@Test
	public void testCallingReportWhenScheduledIsPastErrorResponse() throws Exception {
		assertIsConnected();

		List<WorkInfo> initialWorkList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(0, initialWorkList.size());

		long t_dummy = setTDummyToDaysFromNow(-1);
		AtomicInteger requestCounter = new AtomicInteger(0);

		server.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				requestCounter.getAndIncrement();
				return new MockResponse().setResponseCode(503);
			}
		});

		FakeWorker.safeStartFakeWorker(context);
		WorkInfo workInfo = executeWorker();

		// The worker should be done (success) and there is a new worker enqueued.
		// T_dummy stays the same and exactly one network request is executed (and fails with Error code 503)
		assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
		WorkInfo workInfoNext = null;
		int workerCount = 0;
		for (WorkInfo wi : WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get()) {
			if (wi.getState() == WorkInfo.State.ENQUEUED) {
				workInfoNext = wi;
			}
			workerCount++;
		}
		assertEquals(2, workerCount);
		assertEquals(WorkInfo.State.ENQUEUED, workInfoNext.getState());
		assertEquals(1, requestCounter.get());
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();
		assertEquals(new_t_dummy, t_dummy);
	}

	@Test
	public void testCallingReportWhenScheduledIs2DaysPast() throws Exception {
		assertIsConnected();

		List<WorkInfo> initialWorkList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(0, initialWorkList.size());

		long t_dummy = setTDummyToDaysFromNow(-2);

		AtomicInteger requestCounter = new AtomicInteger(0);

		server.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				requestCounter.getAndIncrement();
				return new MockResponse().setResponseCode(200);
			}
		});

		FakeWorker.safeStartFakeWorker(context);
		WorkInfo workInfo = executeWorker();
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();

		// The worker succeeds by dropping the request and creating and executing 0 or more new requests. The new_t_dummy must be
		// greater the the old t_dummy.
		assertTrue(new_t_dummy > t_dummy);
		assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
	}

	@Test
	public void testSyncInterval() {
		int iterations = 10000;
		long sum = 0;
		for (int i = 0; i < iterations; i++) {
			sum += FakeWorker.clock.syncInterval();
		}
		double averageIntervalDays = (double) (sum / iterations) / 1000 / 60 / 60 / 24;

		double max = 1.1 / FakeWorker.SAMPLING_RATE;
		double min = 0.9 / FakeWorker.SAMPLING_RATE;

		assertTrue(averageIntervalDays < max);
		assertTrue(averageIntervalDays > min);
	}

	@Test
	public void testCallingReportMultipleDays() throws Exception {
		assertIsConnected();

		List<WorkInfo> initialWorkList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(0, initialWorkList.size());

		AtomicInteger requestCounter = new AtomicInteger(0);

		server.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				requestCounter.getAndIncrement();
				return new MockResponse().setResponseCode(200);
			}
		});

		TestClockImpl clock = new TestClockImpl();

		//Initial start -> No request should be executed because the syncInterval is set to 2 days in the future and therefore the
		// first request is scheduled in two days.
		clock.setNextSyncInterval(2);
		FakeWorker.safeStartFakeWorker(context, clock);
		executeWorker();
		assertEquals(0, requestCounter.get());

		//Set Time to two days in the future -> Request should be executed.
		clock.setClockOffset(2);
		executeWorker();
		assertEquals(1, requestCounter.get());
		requestCounter.set(0);

		//At this time one request should be enqueued with execution scheduled in 4 days in the future (2 + 2)

		// Set Time to six days in the future and the nextSyncInterval to 0.5 days -> The currently enqueued request should be
		// dropped and 4 new requests should be executed, because they all fit within the time window of 48 hours.
		clock.setNextSyncInterval(0.5);
		clock.setClockOffset(6);
		executeWorker();
		assertEquals(4, requestCounter.get());
		requestCounter.set(0);

		//At this point one request is scheduled half a day after current time.

		//Setting the Sync interval to 5
		clock.setNextSyncInterval(5);

		//Executing the worker for 100 consecutive days should have the consequence of 20 requests being made.
		for (int i = 7; i < 107; i++) {
			clock.setClockOffset(i);
			executeWorker();
		}
		assertEquals(20, requestCounter.get());
	}

	private WorkInfo executeWorker() throws Exception {
		List<WorkInfo> workInfoList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get().stream()
				.filter(job -> job.getState() == WorkInfo.State.ENQUEUED).collect(Collectors.toList());
		assertEquals(1, workInfoList.size());
		UUID requestID = workInfoList.get(0).getId();
		testDriver.setInitialDelayMet(requestID);
		testDriver.setAllConstraintsMet(requestID);
		WorkInfo workInfo = null;
		do {
			Thread.sleep(500);
			workInfo = WorkManager.getInstance(context).getWorkInfoById(requestID).get();
		} while (workInfo.getState() == WorkInfo.State.RUNNING);
		return workInfo;
	}


	private class TestClockImpl implements FakeWorker.Clock {
		private long clockOffset = 0;
		private long nextSyncInterval = 0;

		public long syncInterval() {
			return nextSyncInterval;
		}

		public void setNextSyncInterval(double days) {
			nextSyncInterval = (long) (days * 24 * 60 * 60 * 1000);
		}

		public void setClockOffset(double days) {
			clockOffset = (long) (days * 24 * 60 * 60 * 1000);
		}

		public long currentTimeMillis() {
			return System.currentTimeMillis() + clockOffset;
		}

	}

	private long setTDummyToDaysFromNow(int daysFromNow) {
		long t_dummy = System.currentTimeMillis() + daysFromNow * 24 * 60 * 60 * 1000;
		SecureStorage.getInstance(context).setTDummy(t_dummy);
		return t_dummy;
	}

}
