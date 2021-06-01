package ch.admin.bag.dp3t.checkin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.checkinflow.CheckOutFragment
import ch.admin.bag.dp3t.checkin.checkinflow.QrCodeScannerFragment
import ch.admin.bag.dp3t.checkin.diary.DiaryFragment
import ch.admin.bag.dp3t.checkin.generateqrcode.EventsOverviewFragment
import ch.admin.bag.dp3t.databinding.FragmentCheckinOverviewBinding
import ch.admin.bag.dp3t.util.StringUtil
import ch.admin.bag.dp3t.extensions.showFragment
import ch.admin.bag.dp3t.viewmodel.TracingViewModel

class CheckinOverviewFragment : Fragment() {

	companion object {
		@JvmStatic
		fun newInstance(): CheckinOverviewFragment {
			return CheckinOverviewFragment()
		}
	}

	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()
	private val tracingViewModel: TracingViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentCheckinOverviewBinding.inflate(inflater).apply {
			checkinOverviewToolbar.setNavigationOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

			checkinOverviewDiary.setOnClickListener { authenticateAndShowDiary() }

			tracingViewModel.appStatusLiveData.observe(viewLifecycleOwner) {
				checkinOverviewIsolation.isVisible = it.isReportedAsInfected
				checkinOverviewScanQr.isVisible = !it.isReportedAsInfected
			}

			crowdNotifierViewModel.isCheckedIn.observe(viewLifecycleOwner, { isCheckedIn ->
				checkoutView.isVisible = isCheckedIn
				checkinView.isVisible = !isCheckedIn
				if (isCheckedIn) {
					val venueInfo = crowdNotifierViewModel.checkInState.venueInfo
					crowdNotifierViewModel.startCheckInTimer()
					checkinTitle.text = venueInfo.description
				}
			})

			checkinButton.setOnClickListener { showFragment(QrCodeScannerFragment.newInstance()) }
			checkoutButton.setOnClickListener { showFragment(CheckOutFragment.newInstance(), modalAnimation = true) }

			crowdNotifierViewModel.timeSinceCheckIn.observe(viewLifecycleOwner) { duration ->
				checkinTime.text = StringUtil.getShortDurationString(duration)
			}

			qrCodeGenerate.root.setOnClickListener { showFragment(EventsOverviewFragment.newInstance()) }

		}.root
	}

	private fun authenticateAndShowDiary() {
		val biometricState = BiometricManager.from(requireContext())
			.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK)
		if (biometricState == BiometricManager.BIOMETRIC_SUCCESS) {
			val executor = ContextCompat.getMainExecutor(requireContext())
			val biometricPrompt = BiometricPrompt(requireActivity(), executor, object : BiometricPrompt.AuthenticationCallback() {
				override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
					showFragment(DiaryFragment.newInstance())
				}
			})
			val promptInfo = BiometricPrompt.PromptInfo.Builder()
				.setTitle(getString(R.string.authenticate_for_diary))
				.setAllowedAuthenticators(
					BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK
				)
				.build()
			biometricPrompt.authenticate(promptInfo)
		} else {
			showFragment(DiaryFragment.newInstance())
		}
	}

}