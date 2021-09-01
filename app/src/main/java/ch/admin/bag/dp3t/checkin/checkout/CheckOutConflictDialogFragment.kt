package ch.admin.bag.dp3t.checkin.checkout

import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.checkout.CheckoutConflictRecyclerViewAdapter.ConflictingVenueVisitItem
import ch.admin.bag.dp3t.checkin.diary.EditDiaryEntryFragment
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.databinding.DialogFragmentCheckoutConflictBinding
import ch.admin.bag.dp3t.extensions.replace
import ch.admin.bag.dp3t.extensions.showFragment
import org.crowdnotifier.android.sdk.model.VenueInfo
import java.util.regex.Pattern

class CheckOutConflictDialogFragment : DialogFragment() {

	companion object {
		val TAG = CheckOutConflictDialogFragment::class.java.canonicalName

		private const val ARG_CHECKIN_TIME = "CHECKIN_TIME"
		private const val ARG_CHECKOUT_TIME = "CHECKOUT_TIME"

		fun newInstance(checkinTime: Long, checkoutTime: Long): CheckOutConflictDialogFragment {
			val fragment = CheckOutConflictDialogFragment()
			fragment.arguments = Bundle().apply {
				putLong(ARG_CHECKIN_TIME, checkinTime)
				putLong(ARG_CHECKOUT_TIME, checkoutTime)
			}
			return fragment
		}
	}

	private val viewModel: CrowdNotifierViewModel by activityViewModels()

	private lateinit var venueInfo: VenueInfo
	private lateinit var checkInState: CheckInState

	private lateinit var diaryStorage: DiaryStorage

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		diaryStorage = DiaryStorage.getInstance(context)

		checkInState = viewModel.checkInState?.copy(
			checkInTime = requireArguments().getLong(ARG_CHECKIN_TIME),
			checkOutTime = requireArguments().getLong(ARG_CHECKOUT_TIME)
		) ?: return
		venueInfo = checkInState.venueInfo
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return DialogFragmentCheckoutConflictBinding.inflate(inflater, container, false).apply {
			checkoutConflictText.text =
				SpannableString(getText(R.string.checkin_overlap_popup_text)).replace(Pattern.compile("\\{CHECKIN\\}")) { _, _ ->
					SpannableStringBuilder().bold { append(venueInfo.title) }
				}

			val conflictingItems = diaryStorage.checkForOverlap(checkInState)
				.sortedBy { it.checkInTime }
				.map { diaryEntry ->
					ConflictingVenueVisitItem(diaryEntry) {
						viewModel.isResolvingCheckoutConflicts = true
						dismiss()
						showFragment(EditDiaryEntryFragment.newInstance(diaryEntry.id), modalAnimation = true)
					}
				}

			checkoutConflictList.adapter = CheckoutConflictRecyclerViewAdapter().apply {
				setData(conflictingItems)
			}

			checkoutConflictBackButton.setOnClickListener {
				viewModel.isResolvingCheckoutConflicts = false
				dismiss()
			}
		}.root
	}

	override fun onCancel(dialog: DialogInterface) {
		super.onCancel(dialog)
		viewModel.isResolvingCheckoutConflicts = false
	}

	override fun onResume() {
		requireDialog().window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		super.onResume()
	}

}