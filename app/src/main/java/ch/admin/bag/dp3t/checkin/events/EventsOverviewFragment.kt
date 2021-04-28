package ch.admin.bag.dp3t.checkin.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.generateqrcode.GenerateQrCodeFragment
import ch.admin.bag.dp3t.databinding.FragmentEventsOverviewBinding

class EventsOverviewFragment : Fragment() {

	companion object {
		fun newInstance(): EventsOverviewFragment {
			return EventsOverviewFragment()
		}
	}

	private lateinit var binding: FragmentEventsOverviewBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		binding = FragmentEventsOverviewBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = QrCodeAdapter(object : OnClickListener {
			override fun generateQrCode() {
				requireActivity().supportFragmentManager.beginTransaction()
					.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
					.replace(R.id.main_fragment_container, GenerateQrCodeFragment.newInstance())
					.addToBackStack(GenerateQrCodeFragment::class.java.canonicalName)
					.commit()
			}

			override fun onQrCodeClicked(qrCodeItem: QrCodeItem) {
				TODO("Not yet implemented")
			}

		})
		binding.qrList.adapter = adapter
		adapter.setItems(emptyList())
	}


}