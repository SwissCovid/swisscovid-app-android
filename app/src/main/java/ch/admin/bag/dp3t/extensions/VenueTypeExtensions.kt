package ch.admin.bag.dp3t.extensions

import androidx.annotation.StringRes
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.VenueType

@StringRes
fun VenueType.getNameRes(): Int {
	return when (this) {
		VenueType.MEETING_ROOM -> R.string.web_generator_category_meeting_room
		VenueType.CAFETERIA -> R.string.web_generator_category_cafeteria
		VenueType.PRIVATE_EVENT -> R.string.web_generator_category_private_event
		VenueType.CANTEEN -> R.string.web_generator_category_canteen
		VenueType.LIBRARY -> R.string.web_generator_category_library
		VenueType.LECTURE_ROOM -> R.string.web_generator_category_lecture_room
		VenueType.SHOP -> R.string.web_generator_category_shop
		VenueType.GYM -> R.string.web_generator_category_gym
		VenueType.KITCHEN_AREA -> R.string.web_generator_category_kitchen_area
		VenueType.OFFICE_SPACE -> R.string.web_generator_category_office_space
		else -> R.string.web_generator_category_other
	}
}