package ch.admin.bag.dp3t.util;

import androidx.annotation.DrawableRes;


import com.google.protobuf.InvalidProtocolBufferException;

import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.models.Proto;

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

	@DrawableRes
	public static int getVenueTypeDrawable(VenueInfo venueInfo) {
		return getDrawableForVenueType(getNotifyMeLocationData(venueInfo).getType());
	}

	@DrawableRes
	public static int getDrawableForVenueType(Proto.VenueType venueType) {
		switch (venueType) {
			case CAFETERIA:
				return R.drawable.ic_illus_caffeteria;
			case MEETING_ROOM:
				return R.drawable.ic_illus_meeting;
			case PRIVATE_EVENT:
				return R.drawable.ic_illus_private_event;
			case CANTEEN:
				return R.drawable.ic_illus_canteen;
			case LIBRARY:
				return R.drawable.ic_illus_library;
			case LECTURE_ROOM:
				return R.drawable.ic_illus_lecture_room;
			case SHOP:
				return R.drawable.ic_illus_shop;
			case GYM:
				return R.drawable.ic_illus_gym;
			case KITCHEN_AREA:
				return R.drawable.ic_illus_kitchen_area;
			case OFFICE_SPACE:
				return R.drawable.ic_illus_office_space;
			default:
				return R.drawable.ic_illus_other;
		}
	}

}
