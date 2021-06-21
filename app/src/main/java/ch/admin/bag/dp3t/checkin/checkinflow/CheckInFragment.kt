package ch.admin.bag.dp3t.checkin.checkinflow

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CheckinOverviewFragment
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.generateqrcode.EventsOverviewFragment
import ch.admin.bag.dp3t.checkin.models.ReminderOption
import ch.admin.bag.dp3t.databinding.FragmentCheckInBinding
import ch.admin.bag.dp3t.extensions.getReminderDelayOptions
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_IS_SELF_CHECKIN = "ARG_IS_SELF_CHECKIN"

class CheckInFragment : Fragment() {

	companion object {
		val TAG = CheckInFragment::class.java.canonicalName

		@JvmStatic
		fun newInstance(isSelfCheckin: Boolean = false) = CheckInFragment().apply {
			arguments = bundleOf(ARG_IS_SELF_CHECKIN to isSelfCheckin)
		}

	}

	private val viewModel: CrowdNotifierViewModel by activityViewModels()

	private var selectedCheckinTime = MutableLiveData(System.currentTimeMillis())

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
			checkInButton.setOnClickListener {
				viewModel.performCheckinAndSetReminders(venueInfo, selectedCheckinTime.value!!, viewModel.selectedReminderDelay)
				popBackToHomeFragment()
			}

			selectedCheckinTime.observe(viewLifecycleOwner) { selectedTime ->
				val formattedTime = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(Date(selectedTime))
				checkinTime.text = getString(R.string.date_today) + ", " + formattedTime
			}
			checkinTime.setOnClickListener {
				selectCheckinTime()
			}

			reminderToggleGroup.removeAllViews()
			val reminderOptions = venueInfo.getReminderDelayOptions(requireContext()).toMutableList()
			reminderOptions.add(0, ReminderOption(0L))
			for (option in reminderOptions) {
				val toggleButton =
					MaterialButton(ContextThemeWrapper(requireContext(), R.style.NextStep_ToggleButton), null, 0)
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
			cancelButton.setOnClickListener {
				requireActivity().supportFragmentManager.popBackStack(EventsOverviewFragment::class.java.canonicalName, 0)
			}
			selfCheckinToolbar.isVisible = requireArguments().getBoolean(ARG_IS_SELF_CHECKIN)
			toolbar.isVisible = !requireArguments().getBoolean(ARG_IS_SELF_CHECKIN)
		}.root
	}

	private fun selectCheckinTime() {
		val cal = Calendar.getInstance()
		cal.timeInMillis = selectedCheckinTime.value!!
		TimePickerDialog(
			requireContext(),
			{ _, hourOfDay, minute ->
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
				cal.set(Calendar.MINUTE, minute)
				selectedCheckinTime.value = cal.timeInMillis.coerceAtMost(System.currentTimeMillis())
			},
			cal.get(Calendar.HOUR_OF_DAY),
			cal.get(Calendar.MINUTE),
			true
		).show()
	}

	private fun popBackToHomeFragment() {
		if (requireArguments().getBoolean(ARG_IS_SELF_CHECKIN)) {
			requireActivity().supportFragmentManager.popBackStack(CheckinOverviewFragment::class.java.canonicalName, 0)
		} else {
			requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
		}
	}

}