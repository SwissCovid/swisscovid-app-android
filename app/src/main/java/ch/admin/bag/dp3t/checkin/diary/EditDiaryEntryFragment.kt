package ch.admin.bag.dp3t.checkin.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.databinding.FragmentCheckOutAndEditBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import ch.admin.bag.dp3t.extensions.getSwissCovidLocationData
import org.crowdnotifier.android.sdk.CrowdNotifier

class EditDiaryEntryFragment : Fragment() {

	companion object {
		private val TAG = EditDiaryEntryFragment::class.java.canonicalName

		private const val ARG_DIARY_ENTRY_ID = "ARG_DIARY_ENTRY_ID"

		@JvmStatic
		fun newInstance(diaryEntryId: Long) = EditDiaryEntryFragment().apply {
			arguments = bundleOf(ARG_DIARY_ENTRY_ID to diaryEntryId)
		}
	}

	private lateinit var diaryStorage: DiaryStorage
	private lateinit var diaryEntry: DiaryEntry

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		diaryStorage = DiaryStorage.getInstance(requireContext())
		val diaryEntryId = requireArguments().getLong(ARG_DIARY_ENTRY_ID)
		diaryEntry = diaryStorage.getDiaryEntryWithId(diaryEntryId)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentCheckOutAndEditBinding.inflate(inflater, container, false).apply {
			checkoutTitle.text = diaryEntry.venueInfo.title
			checkoutSubtitle.setText(diaryEntry.venueInfo.getSubtitle())

			toolbarDoneButton.setOnClickListener { performSave() }
			toolbarCancelButton.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
			checkoutPrimaryButton.setText(R.string.remove_from_diary_button)
			checkoutPrimaryButton.setOnClickListener { hideInDiary() }

			checkoutTimeArrival.setDateTime(diaryEntry.arrivalTime)
			checkoutTimeArrival.setOnDateTimeChangedListener {
				diaryEntry.arrivalTime = checkoutTimeArrival.getSelectedUnixTimestamp()
			}

			checkoutTimeDeparture.setDateTime(diaryEntry.departureTime)
			checkoutTimeDeparture.setOnDateTimeChangedListener {
				diaryEntry.departureTime = checkoutTimeDeparture.getSelectedUnixTimestamp()
			}
		}.root
	}

	private fun performSave() {
		diaryEntry.run {
			if (arrivalTime > departureTime) {
				// swap arrival and departure time
				arrivalTime = departureTime.also { departureTime = arrivalTime }
			}
		}

		val hasOverlapWithOtherCheckin = CheckinTimeHelper.checkForOverlap(diaryEntry, requireContext())
		if (hasOverlapWithOtherCheckin) {
			CheckinTimeHelper.showSavingNotPossibleDialog(
				getString(R.string.checkout_overlapping_alert_description),
				requireContext()
			)
			return
		}

		val checkinDuration = diaryEntry.departureTime - diaryEntry.arrivalTime
		val maxCheckinTime = diaryEntry.venueInfo.getSwissCovidLocationData().automaticCheckoutDelaylMs
		if (checkinDuration > maxCheckinTime) {
			val dialogText = CheckinTimeHelper.getMaxCheckinTimeExceededMessage(maxCheckinTime, requireContext())
			CheckinTimeHelper.showSavingNotPossibleDialog(dialogText, requireContext())
			return
		}

		saveEntry()
		requireActivity().supportFragmentManager.popBackStack()
	}

	private fun saveEntry() {
		diaryEntry.run {
			CrowdNotifier.updateCheckIn(id, arrivalTime, departureTime, venueInfo, context)
		}
		diaryStorage.updateEntry(diaryEntry)
	}

	private fun hideInDiary() {
		requireActivity().supportFragmentManager.beginTransaction()
			.add(HideInDiaryDialogFragment.newInstance(diaryEntry.id), HideInDiaryDialogFragment.TAG)
			.commit()
	}

}