package ch.admin.bag.dp3t;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.IOException;

import org.crowdnotifier.android.sdk.model.VenueInfo;
import org.crowdnotifier.android.sdk.utils.QrUtils;
import org.dpppt.android.sdk.internal.logger.LogLevel;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.admin.bag.dp3t.checkin.models.CheckInState;
import ch.admin.bag.dp3t.checkin.models.DiaryEntry;
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage;
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper;
import ch.admin.bag.dp3t.extensions.VenueInfoExtensionsKt;
import ch.admin.bag.dp3t.storage.SecureStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AutoCheckoutTest {

	Context context;
	DiaryStorage diaryStorage;
	VenueInfo checkinVenueInfo;

	@Before
	public void setup() throws IOException, QrUtils.InvalidQRCodeFormatException {
		context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Logger.init(context, LogLevel.DEBUG);
		diaryStorage = DiaryStorage.getInstance(context);
		diaryStorage.clear();

		checkinVenueInfo = QrUtils.getVenueInfoFromQrCode("CAQSFAgEEgRUZXN0IL3drYYGKL29nZ4mGoYBCAQSYJVub6E0VUfo4GDIli3dOIY78shUBu0DsgS8NA-12wEpapYNAL4kDKoI2wAWZPT3Aoqdu7M66hcr_9WLSmRPHss7e77TeKinyXVqyLS0c0bY2_N6Yjd3A7f8jaO7IqIUFRogzt1hjKGnZoXWLtf-Oqmx755rZDjLagUig00M4lBkfFAiHwgEIIDo3Q0ogNzMFDDg1AMwgN3bATCAurcDMID07gY");
	}

	@Test
	public void checkAutoCheckoutWithEmptyDiary(){

		long checkinTime = System.currentTimeMillis()-13*60*60*1000L;
		CheckInState checkInState = new CheckInState(true, checkinVenueInfo, checkinTime, -1, -1);
		CrowdNotifierReminderHelper.autoCheckoutIfNecessary(context, checkInState);

		// There should be exactly one entry in the diary
		assertEquals(1, diaryStorage.getEntries().size());
		assertEquals(checkinTime, diaryStorage.getEntries().get(0).getCheckInTime());
		assertEquals(checkinTime+ VenueInfoExtensionsKt.getAutoCheckoutDelay(checkinVenueInfo), diaryStorage.getEntries().get(0).getCheckOutTime());
	}

	@Test
	public void checkAutoCheckoutWithoutOverlap(){

		long checkinTime = System.currentTimeMillis()-13*60*60*1000L;

		diaryStorage.addEntry(new DiaryEntry(0, checkinTime-60*60*1000L, checkinTime, checkinVenueInfo));

		CheckInState checkInState = new CheckInState(true, checkinVenueInfo, checkinTime, -1, -1);
		CrowdNotifierReminderHelper.autoCheckoutIfNecessary(context, checkInState);

		// There should be exactly one entry in the diary
		assertEquals(2, diaryStorage.getEntries().size());
		assertEquals(checkinTime, diaryStorage.getEntries().get(1).getCheckInTime());
		assertEquals(checkinTime+ VenueInfoExtensionsKt.getAutoCheckoutDelay(checkinVenueInfo), diaryStorage.getEntries().get(1).getCheckOutTime());
	}

	@Test
	public void checkAutoCheckoutWithOneOverlap(){

		long checkinTime = System.currentTimeMillis()-13*60*60*1000L;
		long overlapCheckin = checkinTime+60*60*1000L;
		long overlapCheckout = overlapCheckin+60*60*1000L;

		diaryStorage.addEntry(new DiaryEntry(0, overlapCheckin, overlapCheckout, checkinVenueInfo));

		CheckInState checkInState = new CheckInState(true, checkinVenueInfo, checkinTime, -1, -1);
		CrowdNotifierReminderHelper.autoCheckoutIfNecessary(context, checkInState);

		// There should be exactly one entry in the diary
		assertEquals(3, diaryStorage.getEntries().size());
		assertEquals(checkinTime, diaryStorage.getEntries().get(1).getCheckInTime());
		assertEquals(overlapCheckin, diaryStorage.getEntries().get(1).getCheckOutTime());
		assertEquals(overlapCheckout, diaryStorage.getEntries().get(2).getCheckInTime());
		assertEquals(checkinTime+ VenueInfoExtensionsKt.getAutoCheckoutDelay(checkinVenueInfo), diaryStorage.getEntries().get(2).getCheckOutTime());
	}

	@Test
	public void checkAutoCheckoutWithTwoOverlapsNoGap(){

		long checkinTime = System.currentTimeMillis()-13*60*60*1000L;
		long overlap1Checkin = checkinTime+60*60*1000L;
		long overlap1Checkout = overlap1Checkin+60*60*1000L;
		long overlap2Checkin = overlap1Checkout;
		long overlap2Checkout = overlap1Checkout+2*60*60*1000L;


		diaryStorage.addEntry(new DiaryEntry(0, overlap1Checkin, overlap1Checkout, checkinVenueInfo));
		diaryStorage.addEntry(new DiaryEntry(1, overlap2Checkin, overlap2Checkout, checkinVenueInfo));

		CheckInState checkInState = new CheckInState(true, checkinVenueInfo, checkinTime, -1, -1);
		CrowdNotifierReminderHelper.autoCheckoutIfNecessary(context, checkInState);

		// There should be exactly one entry in the diary
		assertEquals(4, diaryStorage.getEntries().size());
		assertEquals(checkinTime, diaryStorage.getEntries().get(2).getCheckInTime());
		assertEquals(overlap1Checkin, diaryStorage.getEntries().get(2).getCheckOutTime());
		assertEquals(overlap2Checkout, diaryStorage.getEntries().get(3).getCheckInTime());
		assertEquals(checkinTime+ VenueInfoExtensionsKt.getAutoCheckoutDelay(checkinVenueInfo), diaryStorage.getEntries().get(3).getCheckOutTime());

	}

	@Test
	public void checkAutoCheckoutWithTwoOverlapsWithGap(){

		long checkinTime = System.currentTimeMillis()-13*60*60*1000L;
		long overlap1Checkin = checkinTime+60*60*1000L;
		long overlap1Checkout = overlap1Checkin+60*60*1000L;
		long overlap2Checkin = overlap1Checkout+60*60*1000L;
		long overlap2Checkout = overlap1Checkout+2*60*60*1000L;


		diaryStorage.addEntry(new DiaryEntry(0, overlap1Checkin, overlap1Checkout, checkinVenueInfo));
		diaryStorage.addEntry(new DiaryEntry(1, overlap2Checkin, overlap2Checkout, checkinVenueInfo));

		CheckInState checkInState = new CheckInState(true, checkinVenueInfo, checkinTime, -1, -1);
		CrowdNotifierReminderHelper.autoCheckoutIfNecessary(context, checkInState);

		// There should be exactly one entry in the diary
		assertEquals(5, diaryStorage.getEntries().size());
		assertEquals(checkinTime, diaryStorage.getEntries().get(2).getCheckInTime());
		assertEquals(overlap1Checkin, diaryStorage.getEntries().get(2).getCheckOutTime());
		assertEquals(overlap1Checkout, diaryStorage.getEntries().get(3).getCheckInTime());
		assertEquals(overlap2Checkin, diaryStorage.getEntries().get(3).getCheckOutTime());
		assertEquals(overlap2Checkout, diaryStorage.getEntries().get(4).getCheckInTime());
		assertEquals(checkinTime+ VenueInfoExtensionsKt.getAutoCheckoutDelay(checkinVenueInfo), diaryStorage.getEntries().get(4).getCheckOutTime());

	}

}
