package ch.admin.bag.dp3t.checkin.diary

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.checkin.utils.CheckInRecord
import ch.admin.bag.dp3t.databinding.FragmentCheckOutAndEditBinding
import org.crowdnotifier.android.sdk.CrowdNotifier

class EditDiaryEntryFragment : EditCheckinBaseFragment() {

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

	override val checkinRecord: CheckInRecord
		get() = diaryEntry

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		diaryStorage = DiaryStorage.getInstance(requireContext())
		val diaryEntryId = requireArguments().getLong(ARG_DIARY_ENTRY_ID)
		diaryEntry = diaryStorage.getDiaryEntryWithId(diaryEntryId)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		FragmentCheckOutAndEditBinding.bind(view).apply {
			toolbarDoneButton.setOnClickListener { performSave() }

			checkoutPrimaryButton.setText(R.string.remove_from_diary_button)
			checkoutPrimaryButton.setOnClickListener { hideInDiary() }
		}
	}

	override fun saveEntry() {
		diaryEntry.run {
			CrowdNotifier.updateCheckIn(id, checkInTime, checkOutTime, venueInfo, context)
		}
		diaryStorage.updateEntry(diaryEntry)

		requireActivity().supportFragmentManager.popBackStack()
	}

	private fun hideInDiary() {
		requireActivity().supportFragmentManager.beginTransaction()
			.add(HideInDiaryDialogFragment.newInstance(diaryEntry.id), HideInDiaryDialogFragment.TAG)
			.commit()
	}

}