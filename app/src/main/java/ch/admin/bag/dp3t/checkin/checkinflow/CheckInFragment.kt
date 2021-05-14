package ch.admin.bag.dp3t.checkin.checkinflow

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.models.ReminderOption
import ch.admin.bag.dp3t.checkin.utils.*
import ch.admin.bag.dp3t.databinding.FragmentCheckInBinding
import com.google.android.material.button.MaterialButton
import org.crowdnotifier.android.sdk.model.VenueInfo

class CheckInFragment : Fragment() {

	companion object {
		val TAG = CheckInFragment::class.java.canonicalName

		@JvmStatic
		fun newInstance(): CheckInFragment {
			return CheckInFragment()
		}
	}

	private val viewModel: CrowdNotifierViewModel by activityViewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		checkIfAutoCheckoutHappened()
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

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentCheckInBinding.inflate(inflater).apply {
			val venueInfo = viewModel.checkInState?.venueInfo ?: return root

			titleTextview.text = venueInfo.title
			subtitleTextview.setText(venueInfo.getSubtitle())
			checkInButton.setOnClickListener {
				performCheckIn(venueInfo)
				popBackToHomeFragment()
			}

			reminderToggleGroup.removeAllViews()
			val reminderOptions = venueInfo.getReminderDelayOptions().toMutableList()
			reminderOptions.add(0, ReminderOption(0L))
			for (option in reminderOptions) {
				val toggleButton =
					MaterialButton(ContextThemeWrapper(requireContext(), R.style.CrowdNotifier_ToggleButton), null, 0)
				toggleButton.text = option.getDisplayString(requireContext())
				toggleButton.tag = option.delayMillis
				reminderToggleGroup.addView(toggleButton, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
				if (viewModel.selectedReminderDelay == option.delayMillis) {
					reminderToggleGroup.check(toggleButton.id)
				}
			}

			reminderToggleGroup.addOnButtonCheckedListener { toggleGroup, checkedId, isChecked ->
				if (isChecked) {
					viewModel.selectedReminderDelay = toggleGroup.findViewById<View>(checkedId).tag as Long
				}
			}

			toolbar.setNavigationOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
		}.root
	}

	private fun performCheckIn(venueInfo: VenueInfo) {
		val checkInTime = System.currentTimeMillis()
		viewModel.startCheckInTimer()
		viewModel.setCheckedIn(true)
		viewModel.checkInState.checkInTime = checkInTime
		NotificationHelper.getInstance(context).startOngoingNotification(checkInTime, venueInfo)
		CrowdNotifierReminderHelper.setCheckoutWarning(checkInTime, venueInfo.getCheckoutWarningDelay(), context)
		CrowdNotifierReminderHelper.setAutoCheckOut(checkInTime, venueInfo.getAutoCheckoutDelay(), context)
		CrowdNotifierReminderHelper.setReminder(checkInTime + viewModel.selectedReminderDelay, context)

	}

	private fun popBackToHomeFragment() {
		requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
	}

}