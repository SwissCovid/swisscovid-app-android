package ch.admin.bag.dp3t.inform

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentNotThankYouBinding
import ch.admin.bag.dp3t.extensions.showFragment

class NotThankYouFragment : TraceKeyShareBaseFragment() {

	companion object {
		fun newInstance() = NotThankYouFragment()
	}

	private lateinit var binding: FragmentNotThankYouBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentNotThankYouBinding.inflate(inflater)
		return binding.apply {
			(requireActivity() as InformActivity).allowBackButton(false)
			dontSendButton.setOnClickListener {
				performUpload(onSuccess = { showFragment(TracingStoppedFragment.newInstance(), R.id.inform_fragment_container) })
			}
			dontSendButton.paintFlags = dontSendButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
			backButton.setOnClickListener { showFragment(ReallyNotShareFragment.newInstance(), R.id.inform_fragment_container) }
		}.root
	}

	override fun setLoadingViewVisible(isVisible: Boolean) {
		binding.apply {
			loadingView.isVisible = isVisible
			dontSendButton.isEnabled = !isVisible
			backButton.isEnabled = !isVisible
		}
	}
}