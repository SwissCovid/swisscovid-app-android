package ch.admin.bag.dp3t.checkin.generateqrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentEventsOverviewBinding
import ch.admin.bag.dp3t.extensions.showFragment
import org.crowdnotifier.android.sdk.model.VenueInfo

class EventsOverviewFragment : Fragment() {

	companion object {
		fun newInstance() = EventsOverviewFragment()
	}

	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentEventsOverviewBinding.inflate(layoutInflater).apply {
			eventsToolbar.setNavigationOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

			val adapter = QrCodeAdapter(object : OnClickListener {
				override fun generateQrCode() {
					showFragment(GenerateQrCodeFragment.newInstance(), modalAnimation = true)
				}

				override fun onQrCodeClicked(qrCodeItem: VenueInfo) {
					showFragment(QrCodeFragment.newInstance(qrCodeItem), modalAnimation = true)
				}
			})

			qrList.adapter = adapter
			qrCodeViewModel.generatedQrCodesLiveData.observe(viewLifecycleOwner) { events ->
				if (events.isEmpty()) {
					adapter.setItems(listOf(ExplanationItem(showOnlyInfobox = false), FooterItem()))
				} else {
					adapter.setItems(events.map { EventItem(it) }.toMutableList<EventOverviewItem>().apply {
						add(0, GenerateQrCodeButtonItem())
						add(ExplanationItem(showOnlyInfobox = true))
						add(FooterItem())
					})
				}
			}
		}.root
	}

}