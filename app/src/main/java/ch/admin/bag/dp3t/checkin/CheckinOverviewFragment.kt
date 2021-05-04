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
import ch.admin.bag.dp3t.databinding.FragmentCheckinOverviewBinding
import ch.admin.bag.dp3t.util.StringUtil

class CheckinOverviewFragment : Fragment() {

	companion object {
		@JvmStatic
		fun newInstance(): CheckinOverviewFragment {
			return CheckinOverviewFragment()
		}
	}

	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentCheckinOverviewBinding.inflate(layoutInflater).apply {
			checkinOverviewToolbar.setNavigationOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

			checkinOverviewDiary.setOnClickListener { authenticateAndShowDiary() }

			crowdNotifierViewModel.isCheckedIn.observe(viewLifecycleOwner, { isCheckedIn ->
				checkoutView.isVisible = isCheckedIn
				checkinView.isVisible = !isCheckedIn
				if (isCheckedIn) {
					crowdNotifierViewModel.startCheckInTimer()
				}
			})

			checkinButton.setOnClickListener { showQrCodeScannerFragment() }
			checkoutButton.setOnClickListener { showCheckOutFragment() }

			crowdNotifierViewModel.timeSinceCheckIn.observe(viewLifecycleOwner) { duration ->
				checkinTime.text = StringUtil.getShortDurationString(duration)
			}
		}.root
	}

	private fun showCheckOutFragment() {
		requireActivity().supportFragmentManager.beginTransaction()
			.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
			.replace(R.id.main_fragment_container, CheckOutFragment.newInstance())
			.addToBackStack(CheckOutFragment::class.java.canonicalName)
			.commit()
	}

	private fun showQrCodeScannerFragment() {
		requireActivity().supportFragmentManager.beginTransaction()
			.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
			.replace(R.id.main_fragment_container, QrCodeScannerFragment.newInstance())
			.addToBackStack(QrCodeScannerFragment::class.java.canonicalName)
			.commit()
	}

	private fun authenticateAndShowDiary() {
		val executor = ContextCompat.getMainExecutor(requireContext())
		val biometricPrompt = BiometricPrompt(requireActivity(), executor, object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				showDiaryFragment()
			}
		})

		if (BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
		) {
			val promptInfo = BiometricPrompt.PromptInfo.Builder()
				.setTitle(getString(R.string.authenticate_for_diary))
				.setAllowedAuthenticators(
					BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK
				)
				.build()
			biometricPrompt.authenticate(promptInfo)
		} else {
			showDiaryFragment()
		}
	}

	private fun showDiaryFragment() {
		requireActivity().supportFragmentManager.beginTransaction()
			.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
			.replace(R.id.main_fragment_container, DiaryFragment.newInstance())
			.addToBackStack(DiaryFragment::class.java.canonicalName)
			.commit()
	}
}