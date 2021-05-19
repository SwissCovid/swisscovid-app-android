package ch.admin.bag.dp3t.checkin.generateqrcode

import org.crowdnotifier.android.sdk.model.VenueInfo

abstract class EventOverviewItem {
	companion object {
		const val TYPE_EVENT = 0
		const val TYPE_EXPLANATION = 1
		const val TYPE_FOOTER = 2
		const val TYPE_GENERATE_QR_CODE_BUTTON = 3
	}

	abstract val type: Int
}

class EventItem(val venueInfo: VenueInfo) : EventOverviewItem() {
	override val type = TYPE_EVENT
}

class ExplanationItem(val showOnlyInfobox: Boolean) : EventOverviewItem() {
	override val type = TYPE_EXPLANATION
}

class FooterItem : EventOverviewItem() {
	override val type = TYPE_FOOTER
}

class GenerateQrCodeButtonItem : EventOverviewItem() {
	override val type = TYPE_GENERATE_QR_CODE_BUTTON
}

