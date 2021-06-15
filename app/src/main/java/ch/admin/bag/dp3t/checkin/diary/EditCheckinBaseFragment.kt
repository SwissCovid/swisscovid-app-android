package ch.admin.bag.dp3t.checkin.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.utils.CheckInRecord
import ch.admin.bag.dp3t.databinding.FragmentCheckOutAndEditBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import ch.admin.bag.dp3t.extensions.getSwissCovidLocationData

abstract class EditCheckinBaseFragment : Fragment() {

	abstract val checkinRecord: CheckInRecord

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentCheckOutAndEditBinding.inflate(inflater, container, false).apply {
			checkoutTitle.text = checkinRecord.venueInfo.title
			checkoutSubtitle.setText(checkinRecord.venueInfo.getSubtitle())

			toolbarCancelButton.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

			checkoutTimeArrival.setDateTime(checkinRecord.checkInTime)
			checkoutTimeArrival.setOnDateTimeChangedListener {
				checkinRecord.checkInTime = checkoutTimeArrival.getSelectedUnixTimestamp()
			}

			checkoutTimeDeparture.setDateTime(checkinRecord.checkOutTime)
			checkoutTimeDeparture.setOnDateTimeChangedListener {
				checkinRecord.checkOutTime = checkoutTimeDeparture.getSelectedUnixTimestamp()
			}
		}.root
	}

	fun performSave() {
		if (checkinRecord.checkInTime > checkinRecord.checkOutTime) {
			CheckinTimeHelper.showSavingNotPossibleDialog(
				getString(R.string.checkout_inverse_time_alert_description),
				requireContext()
			)
			return
		}

		val hasOverlapWithOtherCheckin = CheckinTimeHelper.checkForOverlap(checkinRecord, requireContext())
		if (hasOverlapWithOtherCheckin) {
			CheckinTimeHelper.showSavingNotPossibleDialog(
				getString(R.string.checkout_overlapping_alert_description),
				requireContext()
			)
			return
		}

		val checkinDuration = checkinRecord.checkOutTime - checkinRecord.checkInTime
		val maxCheckinTime = checkinRecord.venueInfo.getSwissCovidLocationData().automaticCheckoutDelaylMs
		if (checkinDuration > maxCheckinTime) {
			val dialogText = CheckinTimeHelper.getMaxCheckinTimeExceededMessage(maxCheckinTime, requireContext())
			CheckinTimeHelper.showSavingNotPossibleDialog(dialogText, requireContext())
			return
		}

		saveEntry()
	}

	abstract fun saveEntry()

}