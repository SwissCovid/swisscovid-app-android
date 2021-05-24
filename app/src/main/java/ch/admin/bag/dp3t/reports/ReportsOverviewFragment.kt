package ch.admin.bag.dp3t.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.databinding.FragmentReportsOverviewBinding
import ch.admin.bag.dp3t.extensions.showFragment
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import org.crowdnotifier.android.sdk.model.ExposureEvent
import org.dpppt.android.sdk.models.ExposureDay

class ReportsOverviewFragment : Fragment() {

	companion object {
		@JvmStatic
		fun newInstance() = ReportsOverviewFragment()
	}

	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()
	private val tracingViewModel: TracingViewModel by activityViewModels()
	private val diaryStorage by lazy { DiaryStorage.getInstance(requireContext()) }
	private val secureStorage by lazy { SecureStorage.getInstance(requireContext()) }

	private lateinit var binding: FragmentReportsOverviewBinding
	private val recyclerAdapter = ReportsRecyclerAdapter()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentReportsOverviewBinding.inflate(inflater)
		return binding.apply {
			reportsToolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
			reportsRecyclerView.adapter = recyclerAdapter

			crowdNotifierViewModel.exposures.observe(viewLifecycleOwner) {
				updateRecyclerList(it, tracingViewModel.tracingStatusInterface.exposureDays)
			}
			tracingViewModel.tracingStatusLiveData.observe(viewLifecycleOwner) { tracingStatus ->
				updateRecyclerList(crowdNotifierViewModel.exposures.value ?: listOf(), tracingStatus.exposureDays)
			}
			recyclerAdapter.setOnClickListener {
				showFragment(ReportsFragment.newInstance(it))
			}
			if (secureStorage.isReportsHeaderAnimationPending) {
				setupSplashScreen()
			}
		}.root
	}

	private fun updateRecyclerList(checkinExposures: List<ExposureEvent>, tracingExposureDays: List<ExposureDay>) {
		val items = checkinExposures.map {
			CheckinReportItem(it, diaryStorage.getDiaryEntryWithId(it.id))
		}.toMutableList<ReportItem>().apply {
			if (tracingExposureDays.isNotEmpty()) {
				add(0, ProximityTracingReportItem(tracingExposureDays))
			}
		}
		if (items.isEmpty()) {
			parentFragmentManager.popBackStack()
		}
		recyclerAdapter.setItems(items)
	}

	private fun setupSplashScreen() {
		binding.splashReport.apply {
			root.isVisible = true
			headerContinueButton.isVisible = true
			headerShowAllButton.isVisible = false
			headerSlogan.isVisible = true
			headerImage.isVisible = true
			headerInfo.isVisible = false
			headerSubtitle.isVisible = false
			headerContinueButton.setOnClickListener { hideSplashScreen() }
		}

	}

	private fun hideSplashScreen() {
		secureStorage.isReportsHeaderAnimationPending = false
		val autoTransition = AutoTransition()
		autoTransition.duration = 300
		TransitionManager.beginDelayedTransition(binding.root, autoTransition)
		binding.splashReport.root.isVisible = false
	}

}