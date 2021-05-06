package ch.admin.bag.dp3t.infotab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.checkin.generateqrcode.EventsOverviewFragment
import ch.admin.bag.dp3t.databinding.FragmentInfoTabBinding
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.travel.TravelFragment
import ch.admin.bag.dp3t.travel.TravelUtils
import ch.admin.bag.dp3t.util.showFragment
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import ch.admin.bag.dp3t.whattodo.WtdPositiveTestFragment
import ch.admin.bag.dp3t.whattodo.WtdSymptomsFragment

class InfoTabFragment : Fragment() {


	companion object {
		@JvmStatic
		fun newInstance() = InfoTabFragment()
	}

	private lateinit var binding: FragmentInfoTabBinding
	private val secureStorage by lazy { SecureStorage.getInstance(requireContext()) }
	private val tracingViewModel: TracingViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentInfoTabBinding.inflate(inflater).apply {
			frameCardSymptoms.root.setOnClickListener { showFragment(WtdSymptomsFragment.newInstance()) }
			frameCardTest.root.setOnClickListener { showFragment(WtdPositiveTestFragment.newInstance()) }
			tracingViewModel.appStatusLiveData.observe(viewLifecycleOwner) { tracingStatusInterface ->
				frameCardSymptoms.root.isVisible = !tracingStatusInterface.isReportedAsInfected
				frameCardTest.root.isVisible = !tracingStatusInterface.isReportedAsInfected
			}
			qrCodeGenerate.setOnClickListener { showFragment(EventsOverviewFragment.newInstance()) }
		}
		setupTravelCard()
		return binding.root
	}


	private fun setupTravelCard() {
		binding.cardTravel.apply {
			cardTravel.setOnClickListener { showFragment(TravelFragment.newInstance()) }
			val countries: List<String> = secureStorage.interopCountries
			cardTravel.isVisible = countries.isNotEmpty()
			if (countries.isNotEmpty()) {
				TravelUtils.inflateFlagFlow(travelFlagsFlow, countries)
			}
		}
	}

}