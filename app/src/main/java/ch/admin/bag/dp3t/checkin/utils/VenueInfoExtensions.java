package ch.admin.bag.dp3t.checkin.utils;


import com.google.protobuf.InvalidProtocolBufferException;

import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.checkin.models.Proto;

public class VenueInfoExtensions {


	public static Proto.SwissCovidLocationData getNotifyMeLocationData(VenueInfo venueInfo) {
		if (venueInfo.getCountryData() == null) return Proto.SwissCovidLocationData.newBuilder().build();
		try {
			return Proto.SwissCovidLocationData.parseFrom(venueInfo.getCountryData());
		} catch (InvalidProtocolBufferException e) {
			return Proto.SwissCovidLocationData.newBuilder().build();
		}
	}

	public static String getSubtitle(VenueInfo venueInfo) {
		Proto.SwissCovidLocationData notifyMeLocationData = getNotifyMeLocationData(venueInfo);
		if (notifyMeLocationData.getRoom() == null || notifyMeLocationData.getRoom().equals("")) {
			return venueInfo.getAddress();
		} else {
			return venueInfo.getAddress() + ", " + notifyMeLocationData.getRoom();
		}
	}

}
