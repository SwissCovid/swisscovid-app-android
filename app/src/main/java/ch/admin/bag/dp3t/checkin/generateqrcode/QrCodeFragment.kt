package ch.admin.bag.dp3t.checkin.generateqrcode

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.checkinflow.CheckInFragment
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.models.QRCodePayload
import ch.admin.bag.dp3t.databinding.FragmentQrCodeBinding
import ch.admin.bag.dp3t.extensions.*
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import com.google.protobuf.ByteString
import org.crowdnotifier.android.sdk.model.VenueInfo
import java.io.File

private const val KEY_VENUE_INFO = "KEY_VENUE_INFO"

class QrCodeFragment : Fragment() {

	companion object {
		fun newInstance(venueInfo: VenueInfo) = QrCodeFragment().apply {
			arguments = bundleOf(KEY_VENUE_INFO to venueInfo.toQrCodePayload().toByteString())
		}
	}

	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()
	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()
	private val tracingViewModel: TracingViewModel by activityViewModels()


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentQrCodeBinding.inflate(layoutInflater).apply {
			cancelButton.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
			val venueInfo = QRCodePayload.parseFrom(arguments?.get(KEY_VENUE_INFO) as ByteString).toVenueInfo()
			titleTextview.text = venueInfo.title
			subtitleTextview.setText(venueInfo.getSubtitle())
			qrCodeImageview.visibility = View.INVISIBLE
			qrCodeLoadingProgressbar.isVisible = true
			shareButton.isEnabled = false
			printPdfButton.isEnabled = false
			qrCodeViewModel.selectedQrCodeBitmap.observe(viewLifecycleOwner) {
				qrCodeImageview.visibility = View.VISIBLE
				qrCodeLoadingProgressbar.isVisible = false
				qrCodeImageview.setImageBitmap(it)
			}
			qrCodeViewModel.selectedQrCodePdf.observe(viewLifecycleOwner) { pdfFile ->
				shareButton.isEnabled = true
				printPdfButton.isEnabled = true
				shareButton.setOnClickListener { sharePdf(pdfFile) }
				printPdfButton.setOnClickListener { printPdf(pdfFile) }
			}
			qrCodeImageview.post {
				qrCodeViewModel.generateQrCodeBitmapAndPdf(venueInfo, qrCodeImageview.width)
			}
			deleteButton.setOnClickListener {
				showDeleteConfirmationDialog(venueInfo)
			}
			crowdNotifierViewModel.isCheckedIn.combineWith(tracingViewModel.appStatusLiveData) { isCheckedIn, appStatusInterface ->
				// show self-checkin button only if user is not in isolation and is not checked in already
				appStatusInterface?.isReportedAsInfected == false && isCheckedIn == false
			}.observe(viewLifecycleOwner) { showCheckinButton ->
				checkinButton.isVisible = showCheckinButton
			}
			checkinButton.setOnClickListener { showCheckInFragment(venueInfo) }
		}.root
	}

	private fun showCheckInFragment(venueInfo: VenueInfo) {
		crowdNotifierViewModel.checkInState =
			CheckInState(false, venueInfo, System.currentTimeMillis(), System.currentTimeMillis(), 0)
		showFragment(CheckInFragment.newInstance(isSelfCheckin = true))
	}

	private fun printPdf(file: File) {
		context?.let {
			val manager = it.getSystemService(Context.PRINT_SERVICE) as PrintManager
			val adapter = PdfPrintDocumentAdapter(file)
			val attributes = PrintAttributes.Builder().build()
			manager.print("SwissCovid QR Code", adapter, attributes)
		}
	}

	private fun sharePdf(file: File) {
		context?.let {
			val pdfUri: Uri = FileProvider.getUriForFile(it, it.applicationContext.packageName.toString() + ".provider", file)

			Intent().apply {
				action = Intent.ACTION_SEND
				type = "application/pdf"
				putExtra(Intent.EXTRA_STREAM, pdfUri)
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivity(this)
			}
		}
	}

	private fun showDeleteConfirmationDialog(venueInfo: VenueInfo) {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.delete_qr_code_dialog)
			.setPositiveButton(R.string.delete_button_title) { _, _ ->
				qrCodeViewModel.deleteQrCode(venueInfo)
				requireActivity().supportFragmentManager.popBackStack()
			}
			.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
			.show()
	}
}