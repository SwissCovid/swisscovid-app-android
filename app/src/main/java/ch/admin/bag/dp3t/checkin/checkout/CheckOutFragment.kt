package ch.admin.bag.dp3t.checkin.checkout

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.EditCheckinBaseFragment
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.models.CheckinInfo
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper
import ch.admin.bag.dp3t.databinding.FragmentCheckOutAndEditBinding
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.model.VenueInfo

class CheckOutFragment : EditCheckinBaseFragment() {

	companion object {
		@JvmStatic
		fun newInstance() = CheckOutFragment()
	}

	private val viewModel: CrowdNotifierViewModel by activityViewModels()

	private lateinit var venueInfo: VenueInfo
	private lateinit var checkInState: CheckInState

	override val checkinInfo: CheckinInfo
		get() = checkInState

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

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		FragmentCheckOutAndEditBinding.bind(view).apply {
			toolbarDoneButton.isVisible = false

			checkoutPrimaryButton.setText(R.string.checkout_button_title)
			checkoutPrimaryButton.setOnClickListener { performSave() }
		}
	}

	override fun handleOverlap(overlappingCheckins: Collection<DiaryEntry>) {
		CheckOutConflictDialogFragment.newInstance(checkInState.checkInTime, checkInState.checkOutTime).show(parentFragmentManager, CheckOutConflictDialogFragment.TAG)
	}

	override fun saveEntry() {
		CrowdNotifierReminderHelper.removeAllReminders(context)

		val checkIn = checkInState.checkInTime
		val checkOut = checkInState.checkOutTime
		val id = CrowdNotifier.addCheckIn(checkIn, checkOut, venueInfo, context)
		DiaryStorage.getInstance(context).addEntry(DiaryEntry(id, checkIn, checkOut, venueInfo))
		viewModel.checkInState = null

		val notificationHelper = NotificationHelper.getInstance(context)
		notificationHelper.stopOngoingNotification()
		notificationHelper.removeReminderNotification()
		popBackToHomeFragment()
	}

	private fun popBackToHomeFragment() {
		requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
	}

}