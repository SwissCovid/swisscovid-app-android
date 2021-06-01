package ch.admin.bag.dp3t.checkin.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisit
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisitDayHeader
import ch.admin.bag.dp3t.checkin.diary.items.VenueVisitRecyclerItem
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.databinding.FragmentCheckinDiaryBinding
import ch.admin.bag.dp3t.extensions.showFragment
import ch.admin.bag.dp3t.reports.CheckinReportItem
import ch.admin.bag.dp3t.reports.ReportsFragment
import ch.admin.bag.dp3t.util.StringUtil
import org.crowdnotifier.android.sdk.model.ExposureEvent
import java.util.*

class DiaryFragment : Fragment() {

	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()

	companion object {
		fun newInstance() = DiaryFragment()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentCheckinDiaryBinding.inflate(inflater, container, false).apply {
			checkinDiaryToolbar.setNavigationOnClickListener {
				parentFragmentManager.popBackStack()
			}

			val recyclerAdapter = DiaryRecyclerViewAdapter()
			checkinDiaryRecyclerView.adapter = recyclerAdapter

			crowdNotifierViewModel.exposures.observe(viewLifecycleOwner) { exposures ->
				val items = ArrayList<VenueVisitRecyclerItem>()
				val diaryEntries = DiaryStorage.getInstance(context).entries.sortedByDescending {
					it.arrivalTime
				}
				val isEmpty = diaryEntries.isEmpty()
				checkinDiaryEmptyView.isVisible = isEmpty
				var daysAgoString = ""
				for (diaryEntry in diaryEntries) {
					val newDaysAgoString: String = StringUtil.getDaysAgoString(diaryEntry.arrivalTime, requireContext())
					if (newDaysAgoString != daysAgoString) {
						daysAgoString = newDaysAgoString
						items.add(ItemVenueVisitDayHeader(daysAgoString))
					}
					items.add(ItemVenueVisit(getExposureWithId(exposures, diaryEntry.id), diaryEntry) {
						onDiaryEntryClicked(diaryEntry, getExposureWithId(exposures, diaryEntry.id))
					})
				}
				recyclerAdapter.setData(items)
			}
		}.root
	}

	private fun getExposureWithId(exposures: List<ExposureEvent>, id: Long): ExposureEvent? {
		return exposures.firstOrNull { it.id == id }
	}

	private fun onDiaryEntryClicked(diaryEntry: DiaryEntry, exposureEvent: ExposureEvent?) {
		if (exposureEvent != null) {
			showFragment(ReportsFragment.newInstance(reportItem = CheckinReportItem(exposureEvent, diaryEntry)))
		} else {
			showFragment(EditDiaryEntryFragment.newInstance(diaryEntry.id), modalAnimation = true)
		}
	}

}