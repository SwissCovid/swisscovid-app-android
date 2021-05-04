package ch.admin.bag.dp3t.checkin.generateqrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.databinding.FragmentQrCodeBinding
import org.crowdnotifier.android.sdk.model.VenueInfo

class QrCodeFragment : Fragment() {

	companion object {
		fun newInstance(venueInfo: VenueInfo) = QrCodeFragment().apply {
			arguments = bundleOf("fef" to venueInfo)
		}
	}

	private lateinit var binding: FragmentQrCodeBinding


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		return FragmentQrCodeBinding.inflate(layoutInflater).apply {
			cancelButton.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
			val venueInfo = arguments?.get("fef") as VenueInfo
		}.root
	}

}