package ch.admin.bag.dp3t.infotab

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.checkinflow.CheckOutFragment
import ch.admin.bag.dp3t.databinding.FragmentInfoTabBinding
import ch.admin.bag.dp3t.extensions.showFragment
import ch.admin.bag.dp3t.inform.InformActivity
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.travel.TravelFragment
import ch.admin.bag.dp3t.travel.TravelUtils
import ch.admin.bag.dp3t.util.UrlUtil
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import ch.admin.bag.dp3t.whattodo.WtdInfolineAccessabilityDialogFragment
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
	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentInfoTabBinding.inflate(inflater).apply {
			frameCardSymptoms.root.setOnClickListener { showFragment(WtdSymptomsFragment.newInstance()) }
			tracingViewModel.appStatusLiveData.observe(viewLifecycleOwner) { tracingStatusInterface ->
				frameCardTest.root.isVisible = !tracingStatusInterface.isReportedAsInfected
			}
		}
		setupTravelCard()
		setupPositiveTestCard()
		return binding.root
	}

	private fun setupPositiveTestCard() {
		fillContentFromConfigServer()
		binding.frameCardTest.apply {
			wtdMoreAboutCovidcodeButton.paintFlags = wtdMoreAboutCovidcodeButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
			wtdMoreAboutCovidcodeButton.setOnClickListener {
				showFragment(WtdPositiveTestFragment.newInstance())
			}
			wtdInformButton.setOnClickListener {
				if (crowdNotifierViewModel.isCheckedIn.value == true) {
					showCannotEnterCovidcodeWhileCheckedInDialog()
				} else {
					startActivity(Intent(activity, InformActivity::class.java))
				}
			}
		}
	}

	private fun showCannotEnterCovidcodeWhileCheckedInDialog() {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.error_cannot_enter_covidcode_while_checked_in)
			.setPositiveButton(R.string.checkout_button_title) { _, _ -> showCheckOutFragment() }
			.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
			.create()
			.show()
	}

	private fun showCheckOutFragment() {
		requireActivity().supportFragmentManager.beginTransaction()
			.setCustomAnimations(R.anim.modal_slide_enter, R.anim.modal_slide_exit, R.anim.modal_pop_enter, R.anim.modal_pop_exit)
			.replace(R.id.main_fragment_container, CheckOutFragment.newInstance())
			.addToBackStack(CheckOutFragment::class.java.canonicalName)
			.commit()
	}

	private fun fillContentFromConfigServer() {
		val textModel = secureStorage.getWhatToDoPositiveTestTexts(getString(R.string.language_key)) ?: return
		binding.frameCardTest.apply {
			wtdInformBoxSupertitle.text = textModel.enterCovidcodeBoxSupertitle
			wtdInformBoxTitle.text = textModel.enterCovidcodeBoxTitle
			wtdInformBoxText.text = textModel.enterCovidcodeBoxText
			wtdInformButton.text = textModel.enterCovidcodeBoxButtonTitle
			wtdInformInfobox.isVisible = textModel.infoBox != null
			textModel.infoBox?.let { infoBox ->
				wtdInformInfoboxTitle.text = infoBox.title
				wtdInformInfoboxMsg.text = infoBox.msg
				if (infoBox.url != null && infoBox.urlTitle != null) {
					wtdInformInfoboxLinkText.text = infoBox.urlTitle
					wtdInformInfoboxLinkLayout.setOnClickListener { UrlUtil.openUrl(it.context, infoBox.url) }
					wtdInformInfoboxLinkLayout.isVisible = true
					if (infoBox.url.startsWith("tel://")) {
						wtdInformInfoboxLinkIcon.setImageResource(R.drawable.ic_phone)
					} else {
						wtdInformInfoboxLinkIcon.setImageResource(R.drawable.ic_launch)
					}
				} else {
					wtdInformInfoboxLinkLayout.isVisible = false
				}
				if (infoBox.hearingImpairedInfo != null) {
					wtdInformInfoboxLinkIcon.setImageResource(R.drawable.ic_phone)
					wtdInformInfoboxLinkHearingImpaired.setOnClickListener { showWtdInfolineAccessabilityDialogFragment(infoBox.hearingImpairedInfo) }
					wtdInformInfoboxLinkHearingImpaired.isVisible = true
				} else {
					wtdInformInfoboxLinkIcon.setImageResource(R.drawable.ic_launch)
					wtdInformInfoboxLinkHearingImpaired.isVisible = false
				}
			}
		}
	}

	private fun showWtdInfolineAccessabilityDialogFragment(hearingImpairedInfo: String) {
		requireActivity().supportFragmentManager.beginTransaction()
			.add(
				WtdInfolineAccessabilityDialogFragment.newInstance(hearingImpairedInfo),
				WtdInfolineAccessabilityDialogFragment::class.java.canonicalName
			)
			.commit()
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