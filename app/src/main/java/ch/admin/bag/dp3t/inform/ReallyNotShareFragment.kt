package ch.admin.bag.dp3t.inform

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.databinding.FragmentInformReallyNotShareBinding
import ch.admin.bag.dp3t.extensions.showFragment

class ReallyNotShareFragment : TraceKeyShareBaseFragment() {

	companion object {
		fun newInstance() = ReallyNotShareFragment()
	}

	private lateinit var binding: FragmentInformReallyNotShareBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentInformReallyNotShareBinding.inflate(inflater).apply {
			(requireActivity() as InformActivity).allowBackButton(false)
			tryAgainButton.setOnClickListener {
				tryAgainButton.isEnabled = false
				showShareTEKsPopup(onSuccess = ::onUserGrantedTEKSharing, onError = ::onUserDidNotGrantTEKSharing)
			}
			dontSendButton.paintFlags = dontSendButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
			dontSendButton.setOnClickListener {
				if (DiaryStorage.getInstance(requireContext()).entries.isNotEmpty()) {
					showFragment(ShareCheckinsFragment.newInstance(), R.id.inform_fragment_container)
				} else {
					showFragment(NotThankYouFragment.newInstance(), R.id.inform_fragment_container)
				}
			}
		}
		return binding.root
	}

	private fun onUserGrantedTEKSharing() {
		informViewModel.hasSharedDP3TKeys = true
		if (informViewModel.getSelectableCheckinItems().isEmpty()) {
			performUpload(onSuccess = { showFragment(ThankYouFragment.newInstance(), R.id.inform_fragment_container) })
		} else {
			showFragment(ShareCheckinsFragment.newInstance(), R.id.inform_fragment_container)
		}
	}

	private fun onUserDidNotGrantTEKSharing() {
		binding.tryAgainButton.isEnabled = true
	}

	override fun setLoadingViewVisible(isVisible: Boolean) {
		binding.loadingView.isVisible = isVisible
		binding.tryAgainButton.isEnabled = !isVisible
	}

}