package ch.admin.bag.dp3t.checkin.checkinflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper
import ch.admin.bag.dp3t.databinding.FragmentCheckOutAndEditBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.model.VenueInfo

class CheckOutFragment : Fragment() {

	companion object {
		private val TAG = CheckOutFragment::class.java.canonicalName

		@JvmStatic
		fun newInstance(): CheckOutFragment {
			return CheckOutFragment()
		}
	}

	private lateinit var viewModel: CrowdNotifierViewModel

	private lateinit var venueInfo: VenueInfo
	private lateinit var checkInState: CheckInState

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel = ViewModelProvider(requireActivity()).get(CrowdNotifierViewModel::class.java)
		checkIfAutoCheckoutHappened()
		checkInState = viewModel.checkInState ?: return
		checkInState.checkOutTime = System.currentTimeMillis()
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
		}.root
	}

	private fun performCheckout() {
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