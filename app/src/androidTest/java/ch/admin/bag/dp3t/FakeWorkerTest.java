package ch.admin.bag.dp3t;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnitRunner;
import androidx.work.*;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.TestDriver;
import androidx.work.testing.WorkManagerTestInitHelper;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import kotlin.jvm.Throws;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
		DP3T.init(context, new ApplicationInfo("test", server.url("/bucket/").toString(), server.url("/report/").toString()),
				null);
		appConfigManager.setTracingEnabled(false);
		DP3T.clearData(context);
		DP3T.init(context, new ApplicationInfo("test", server.url("/bucket/").toString(), server.url("/report/").toString()),
				null);
		appConfigManager.setTracingEnabled(true);

		SecureStorage.getInstance(context).setTDummy(-1);
	}

	@Test
	public void testInitialTDummy() throws Exception {
		List<WorkInfo> initialWorkList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(0, initialWorkList.size());

		FakeWorker.safeStartFakeWorker(context);
		// TDummy is initialized to a time in the future.
		assert (SecureStorage.getInstance(context).getTDummy() > System.currentTimeMillis());
	}

	@Test
	public void testCallingReportWhenScheduledIsNotPast() throws Exception {
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
		List<WorkInfo> workInfoList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(1, workInfoList.size());

		UUID requestID = workInfoList.get(0).getId();
		testDriver.setInitialDelayMet(requestID);
		testDriver.setAllConstraintsMet(requestID);
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();
		WorkInfo workInfo = WorkManager.getInstance(context).getWorkInfoById(requestID).get();

		// Worker succeeds by not executing a request. TDummy stays the same.
		assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
		assertEquals(0, requestCounter.get());
		assertEquals(t_dummy, new_t_dummy);
	}

	@Test
	public void testCallingReportWhenScheduledIsPast() throws Exception {
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
		List<WorkInfo> workInfoList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(1, workInfoList.size());

		UUID requestID = workInfoList.get(0).getId();
		testDriver.setInitialDelayMet(requestID);
		testDriver.setAllConstraintsMet(requestID);
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();
		WorkInfo workInfo = WorkManager.getInstance(context).getWorkInfoById(requestID).get();

		// Worker succeeds by executing at least one request. The new_t_dummy must be greater than the old t_dummy.
		assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
		assertTrue(new_t_dummy > t_dummy);
		assertTrue(requestCounter.get() >= 1);
	}

	@Test
	public void testCallingReportWhenScheduledIsPastErrorResponse() throws Exception {
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
		List<WorkInfo> workInfoList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(1, workInfoList.size());

		UUID requestID = workInfoList.get(0).getId();
		testDriver.setInitialDelayMet(requestID);
		testDriver.setAllConstraintsMet(requestID);
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();

		WorkInfo workInfo = WorkManager.getInstance(context).getWorkInfoById(requestID).get();

		// The request stays enqueued. T_dummy stays the same and exactly one network request is executed (and fails with Error
		// code 503)
		assertEquals(WorkInfo.State.ENQUEUED, workInfo.getState());
		assertEquals(1, requestCounter.get());
		assertEquals(new_t_dummy, t_dummy);
	}


	@Test
	public void testCallingReportWhenScheduledIs2DaysPast() throws Exception {
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
		List<WorkInfo> workInfoList = WorkManager.getInstance(context).getWorkInfosByTag(FakeWorker.WORK_TAG).get();
		assertEquals(1, workInfoList.size());

		UUID requestID = workInfoList.get(0).getId();
		testDriver.setInitialDelayMet(requestID);
		testDriver.setAllConstraintsMet(requestID);
		WorkInfo workInfo = WorkManager.getInstance(context).getWorkInfoById(requestID).get();
		long new_t_dummy = SecureStorage.getInstance(context).getTDummy();

		// The worker succeeds by dropping the request and creating and executing 0 or more new requests. The new_t_dummy must be
		// greater the the old t_dummy.
		assertTrue(new_t_dummy > t_dummy);
		assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
	}


	private long setTDummyToDaysFromNow(int daysFromNow) {
		long t_dummy = System.currentTimeMillis() + daysFromNow * 24 * 60 * 60 * 1000;
		SecureStorage.getInstance(context).setTDummy(t_dummy);
		return t_dummy;
	}

}
