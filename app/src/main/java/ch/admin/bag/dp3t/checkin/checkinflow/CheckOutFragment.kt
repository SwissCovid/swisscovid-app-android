package ch.admin.bag.dp3t.checkin.checkinflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.diary.CheckinTimeHelper
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper
import ch.admin.bag.dp3t.databinding.FragmentCheckOutAndEditBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import ch.admin.bag.dp3t.extensions.getSwissCovidLocationData
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.model.VenueInfo

class CheckOutFragment : Fragment() {

	companion object {
		private val TAG = CheckOutFragment::class.java.canonicalName

		@JvmStatic
		fun newInstance() = CheckOutFragment()
	}

	private val viewModel: CrowdNotifierViewModel by activityViewModels()

	private lateinit var venueInfo: VenueInfo
	private lateinit var checkInState: CheckInState

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		checkIfAutoCheckoutHappened()
		checkInState = viewModel.checkInState?.copy(checkOutTime = System.currentTimeMillis()) ?: return
		venueInfo = checkInState.venueInfo
	}

	override fun onStart() {
		super.onStart()
		checkIfAutoCheckoutHappened()
	}

	private fun checkIfAutoCheckoutHappened() {
		if (viewModel.checkInState == null) {
			popBackToHomeFragment()
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentCheckOutAndEditBinding.inflate(inflater, container, false).apply {
			checkoutTitle.text = venueInfo.title
			checkoutSubtitle.setText(venueInfo.getSubtitle())

			toolbarDoneButton.setOnClickListener { performCheckout() }
			toolbarCancelButton.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
			checkoutPrimaryButton.setText(R.string.checkout_button_title)
			checkoutPrimaryButton.setOnClickListener { performCheckout() }

			checkoutTimeArrival.setDateTime(checkInState.checkInTime)
			checkoutTimeArrival.setOnDateTimeChangedListener {
				checkInState.checkInTime = checkoutTimeArrival.getSelectedUnixTimestamp()
			}

			checkoutTimeDeparture.setDateTime(checkInState.checkOutTime)
			checkoutTimeDeparture.setOnDateTimeChangedListener {
				checkInState.checkOutTime = checkoutTimeDeparture.getSelectedUnixTimestamp()
			}
		}.root
	}

	private fun performCheckout() {
		checkInState.run {
			if (checkInTime > checkOutTime) {
				// swap arrival and departure time
				checkInTime = checkOutTime.also { checkOutTime = checkInTime }
			}
		}

		val hasOverlapWithOtherCheckin =
			CheckinTimeHelper.checkForOverlap(checkInState.checkInTime, checkInState.checkOutTime, requireContext())
		if (hasOverlapWithOtherCheckin) {
			CheckinTimeHelper.showSavingNotPossibleDialog(
				getString(R.string.checkout_overlapping_alert_description),
				requireContext()
			)
			return
		}

		val checkinDuration = checkInState.checkOutTime - checkInState.checkInTime
		val maxCheckinTime = checkInState.venueInfo.getSwissCovidLocationData().automaticCheckoutDelaylMs
		if (checkinDuration > maxCheckinTime) {
			val dialogText = CheckinTimeHelper.getMaxCheckinTimeExceededMessage(maxCheckinTime, requireContext())
			CheckinTimeHelper.showSavingNotPossibleDialog(dialogText, requireContext())
			return
		}

		CrowdNotifierReminderHelper.removeAllReminders(context)
		saveEntry()
		val notificationHelper = NotificationHelper.getInstance(context)
		notificationHelper.stopOngoingNotification()
		notificationHelper.removeReminderNotification()
		popBackToHomeFragment()
	}

	private fun saveEntry() {
		val checkIn = checkInState.checkInTime
		val checkOut = checkInState.checkOutTime
		val id = CrowdNotifier.addCheckIn(checkIn, checkOut, venueInfo, context)
		DiaryStorage.getInstance(context).addEntry(DiaryEntry(id, checkIn, checkOut, venueInfo))
		viewModel.checkInState = null
	}

	private fun popBackToHomeFragment() {
		requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
	}

}