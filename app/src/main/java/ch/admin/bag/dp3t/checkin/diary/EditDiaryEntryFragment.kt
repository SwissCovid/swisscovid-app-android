package ch.admin.bag.dp3t.checkin.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.databinding.FragmentCheckOutAndEditBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import org.crowdnotifier.android.sdk.CrowdNotifier

class EditDiaryEntryFragment : Fragment() {

	companion object {
		private val TAG = EditDiaryEntryFragment::class.java.canonicalName

		private const val ARG_DIARY_ENTRY_ID = "ARG_DIARY_ENTRY_ID"

		@JvmStatic
		fun newInstance(diaryEntryId: Long): EditDiaryEntryFragment {
			val fragment = EditDiaryEntryFragment()
			val args = Bundle()
			args.putLong(ARG_DIARY_ENTRY_ID, diaryEntryId)
			fragment.arguments = args
			return fragment
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
		}.root
	}

	private fun performSave() {
		val hasOverlapWithOtherCheckin = CheckinTimeHelper.checkForOverlap(diaryEntry, requireContext())
		if (hasOverlapWithOtherCheckin) {
			CheckinTimeHelper.showOverlapDialog(requireContext())
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