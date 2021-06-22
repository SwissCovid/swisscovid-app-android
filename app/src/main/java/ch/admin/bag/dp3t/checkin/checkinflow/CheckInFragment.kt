package ch.admin.bag.dp3t.checkin.checkinflow

import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
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
import ch.admin.bag.dp3t.databinding.DialogReminderDurationBinding
import ch.admin.bag.dp3t.databinding.FragmentCheckInBinding
import ch.admin.bag.dp3t.extensions.getReminderDelayOptions
import ch.admin.bag.dp3t.extensions.getSwissCovidLocationData
import ch.admin.bag.dp3t.util.StringUtil
import ch.admin.bag.dp3t.util.StringUtil.toHoursString
import ch.admin.bag.dp3t.util.StringUtil.toMinutesString
import ch.admin.bag.dp3t.util.UiUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.shawnlin.numberpicker.NumberPicker
import java.text.SimpleDateFormat
import java.time.Duration
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

	private val maxCheckinDuration: Duration
		get() =
			viewModel.checkInState?.venueInfo?.getSwissCovidLocationData()?.automaticCheckoutDelaylMs?.let { Duration.ofMillis(it) }
				?: Duration.ofHours(24)

	private var selectedCheckinTime = MutableLiveData(System.currentTimeMillis())

	private var selectedReminderButton: MaterialButton? = null
	private lateinit var customReminderButton: MaterialButton

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
				reminderToggleGroup.addView(toggleButton, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
				if (viewModel.selectedReminderDelay == option.delayMillis) {
					reminderToggleGroup.check(toggleButton.id)
					selectedReminderButton = toggleButton
				}
				toggleButton.addOnCheckedChangeListener { button, isChecked ->
					if (isChecked) {
						viewModel.selectedReminderDelay = option.delayMillis
						selectedReminderButton = button
					}
				}
			}

			customReminderButton =
				MaterialButton(ContextThemeWrapper(requireContext(), R.style.NextStep_ToggleButton), null, 0)
			customReminderButton.apply {
				icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_stopwatch)
				iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.anthracite))
				iconTintMode = PorterDuff.Mode.SRC_IN
				iconSize = UiUtils.dpToPx(resources, 16)
				iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
				iconPadding = 0
			}
			reminderToggleGroup.addView(customReminderButton, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
			customReminderButton.addOnCheckedChangeListener { _, isChecked ->
				if (isChecked) {
					showCustomReminderDialog()
				} else {
					invalidateCustomReminderDelayButtonLabel(isChecked)
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
			R.style.NextStep_AlertDialogStyle,
			{ _, hourOfDay, minute ->
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
				cal.set(Calendar.MINUTE, minute)
				selectedCheckinTime.value = cal.timeInMillis
					.coerceAtLeast(System.currentTimeMillis() - maxCheckinDuration.toMillis())
					.coerceAtMost(System.currentTimeMillis())
			},
			cal.get(Calendar.HOUR_OF_DAY),
			cal.get(Calendar.MINUTE),
			true
		).show()
	}

	private fun showCustomReminderDialog() {
		BottomSheetDialog(requireContext()).apply {
			setContentView(DialogReminderDurationBinding.inflate(layoutInflater, null, false).apply {
				dialogCancel.setOnClickListener {
					cancel()
				}
				dialogDone.setOnClickListener {
					viewModel.selectedReminderDelay =
						Duration.ofHours(dialogHourPicker.value.toLong()).plusMinutes(dialogMinutePicker.value.toLong()).toMillis()
					selectedReminderButton = customReminderButton
					invalidateCustomReminderDelayButtonLabel(true)
					dismiss()
				}

				dialogHourPicker.apply {
					minValue = 0
					maxValue = maxCheckinDuration.toHours().toInt() - 1
					wrapSelectorWheel = false
					formatter = NumberPicker.Formatter { it.toLong().toHoursString(context) }

					value = (viewModel.selectedReminderDelay / 1000L / 60L / 60L).toInt()
						.coerceAtMost(dialogHourPicker.maxValue)
				}
				dialogMinutePicker.apply {
					minValue = 0
					maxValue = 59
					wrapSelectorWheel = true
					formatter = NumberPicker.Formatter { it.toLong().toMinutesString(context) }

					value = (viewModel.selectedReminderDelay / 1000L / 60L % 60L).toInt()
				}
			}.root)
			setOnCancelListener {
				// select previous option
				selectedReminderButton?.isChecked = true
			}
			show()
		}
	}

	private fun invalidateCustomReminderDelayButtonLabel(isChecked: Boolean) {
		if (isChecked) {
			customReminderButton.text = StringUtil.getShortDurationStringWithUnits(viewModel.selectedReminderDelay, requireContext())
			customReminderButton.icon = null
		} else {
			customReminderButton.text = null
			customReminderButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_stopwatch)
		}
	}

	private fun popBackToHomeFragment() {
		if (requireArguments().getBoolean(ARG_IS_SELF_CHECKIN)) {
			requireActivity().supportFragmentManager.popBackStack(CheckinOverviewFragment::class.java.canonicalName, 0)
		} else {
			requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
		}
	}

}