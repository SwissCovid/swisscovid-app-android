package ch.admin.bag.dp3t.checkin.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisit
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisitDayHeader
import ch.admin.bag.dp3t.checkin.diary.items.VenueVisitRecyclerItem
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.checkin.utils.DateTimeUtils
import ch.admin.bag.dp3t.databinding.FragmentCheckinDiaryBinding
import ch.admin.bag.dp3t.reports.ReportsFragment
import org.crowdnotifier.android.sdk.model.ExposureEvent
import java.util.*

class DiaryFragment : Fragment() {

	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()

	companion object {
		fun newInstance(): DiaryFragment {
			return DiaryFragment()
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentCheckinDiaryBinding.inflate(inflater, container, false).apply {
			checkinDiaryToolbar.setNavigationOnClickListener {
				parentFragmentManager.popBackStack()
			}

			val recyclerAdapter = DiaryRecyclerViewAdapter()
			checkinDiaryRecyclerView.adapter = recyclerAdapter

			crowdNotifierViewModel.exposures.observe(viewLifecycleOwner) { exposures ->
				val items: ArrayList<VenueVisitRecyclerItem> = ArrayList<VenueVisitRecyclerItem>()
				val diaryEntries: List<DiaryEntry> = DiaryStorage.getInstance(context).entries.sortedByDescending {
					it.arrivalTime
				}
				val isEmpty = diaryEntries.isEmpty()
				checkinDiaryEmptyView.isVisible = isEmpty
				var daysAgoString = ""
				for (diaryEntry in diaryEntries) {
					val newDaysAgoString: String = DateTimeUtils.getDaysAgoString(diaryEntry.getArrivalTime(), context)
					if (newDaysAgoString != daysAgoString) {
						daysAgoString = newDaysAgoString
						items.add(ItemVenueVisitDayHeader(daysAgoString))
					}
					items.add(ItemVenueVisit(getExposureWithId(exposures, diaryEntry.id), diaryEntry) { v ->
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
			requireActivity().supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
				.addToBackStack(ReportsFragment::class.java.canonicalName)
				.commit()
		} else {
			/*requireActivity().supportFragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, EditDiaryEntryFragment.newInstance(true, diaryEntry.id))
				.addToBackStack(EditDiaryEntryFragment::class.java.canonicalName)
				.commit()*/
		}
	}

}