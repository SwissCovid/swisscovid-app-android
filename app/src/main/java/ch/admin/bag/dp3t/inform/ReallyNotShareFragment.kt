package ch.admin.bag.dp3t.inform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentInformReallyNotShareBinding
import ch.admin.bag.dp3t.util.showFragment

class ReallyNotShareFragment : Fragment() {

	companion object {
		fun newInstance() = ReallyNotShareFragment()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentInformReallyNotShareBinding.inflate(inflater).apply {
			tryAgainButton.setOnClickListener {
				//TODO: Implement sharing
			}
			dontSendButton.setOnClickListener {
				showFragment(ShareCheckinsFragment.newInstance(), R.id.inform_fragment_container)
			}
		}.root
	}

}