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

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentReportsOverviewBinding.inflate(inflater)
		return binding.apply {
			reportsToolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
			val adapter = ReportsRecyclerAdapter()
			reportsRecyclerView.adapter = adapter
			crowdNotifierViewModel.exposures.observe(viewLifecycleOwner) { checkinExposures ->

				adapter.setItems(checkinExposures.map {
					CheckinReportItem(it, diaryStorage.getDiaryEntryWithId(it.id))
				}.toMutableList<ReportItem>().apply {
					val tracingExposureDays = tracingViewModel.tracingStatusInterface.exposureDays.reversed()
					if (tracingExposureDays.isNotEmpty()) {
						add(0, ProximityTracingReportItem(tracingExposureDays))
					}
				})
			}
			adapter.setOnClickListener {
				showFragment(ReportsFragment.newInstance(it))
			}
			if (secureStorage.isReportsHeaderAnimationPending) {
				setupSplashScreen()
			}
		}.root
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