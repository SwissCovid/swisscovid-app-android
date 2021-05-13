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
import ch.admin.bag.dp3t.util.showFragment
import org.crowdnotifier.android.sdk.model.VenueInfo

class EventsOverviewFragment : Fragment() {

	companion object {
		fun newInstance() = EventsOverviewFragment()
	}

	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentEventsOverviewBinding.inflate(layoutInflater).apply {
			eventsToolbar.setNavigationOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

			val adapter = QrCodeAdapter(object : OnClickListener {
				override fun generateQrCode() {
					showFragment(GenerateQrCodeFragment.newInstance())
				}

				override fun onQrCodeClicked(qrCodeItem: VenueInfo) {
					showFragment(QrCodeFragment.newInstance(qrCodeItem))
				}

				override fun onDeleteQrCodeClicked(qrCodeItem: VenueInfo) {
					showDeleteConfirmationDialog(qrCodeItem)
				}

			})

			qrList.adapter = adapter
			qrCodeViewModel.generatedQrCodesLiveData.observe(viewLifecycleOwner, {
				adapter.setItems(it)
			})
		}.root
	}

	private fun showDeleteConfirmationDialog(venueInfo: VenueInfo) {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.delete_qr_code_dialog)
			.setPositiveButton(R.string.delete_button_title) { _, _ -> qrCodeViewModel.deleteQrCode(venueInfo) }
			.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
			.show()
	}

}