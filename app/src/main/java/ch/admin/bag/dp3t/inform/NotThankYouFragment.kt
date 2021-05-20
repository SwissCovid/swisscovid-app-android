package ch.admin.bag.dp3t.inform

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentNotThankYouBinding
import ch.admin.bag.dp3t.extensions.showFragment

class NotThankYouFragment : Fragment() {

	companion object {
		fun newInstance() = NotThankYouFragment()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentNotThankYouBinding.inflate(inflater).apply {
			(requireActivity() as InformActivity).allowBackButton(false)
			dontSendButton.setOnClickListener { requireActivity().finish() }
			dontSendButton.paintFlags = dontSendButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
			backButton.setOnClickListener { showFragment(ReallyNotShareFragment.newInstance(), R.id.inform_fragment_container) }
		}.root
	}
}