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
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.checkinflow.CheckInFragment
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.models.ReminderOption
import ch.admin.bag.dp3t.databinding.FragmentQrCodeBinding
import java.io.File


class QrCodeFragment : Fragment() {

	companion object {
		fun newInstance() = QrCodeFragment()
	}

	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()
	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentQrCodeBinding.inflate(layoutInflater).apply {
			cancelButton.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
			qrCodeViewModel.selectedQrCode?.let { venueInfo ->
				titleTextview.text = venueInfo.title
				subtitleTextview.text = "TODO"
				qrCodeViewModel.selectedQrCodeBitmap.observe(viewLifecycleOwner) {
					if (it == null) {
						qrCodeImageview.visibility = View.INVISIBLE
						qrCodeLoadingProgressbar.isVisible = true
						shareButton.isEnabled = false
						printPdfButton.isEnabled = false
					} else {
						qrCodeImageview.visibility = View.VISIBLE
						qrCodeLoadingProgressbar.isVisible = false
						qrCodeImageview.setImageBitmap(it)
						shareButton.isEnabled = true
						printPdfButton.isEnabled = true
					}
				}
				shareButton.setOnClickListener { generatePdf(isPrint = false) }
				printPdfButton.setOnClickListener { generatePdf(isPrint = true) }
				deleteButton.setOnClickListener {
					qrCodeViewModel.deleteQrCode(venueInfo)
					requireActivity().supportFragmentManager.popBackStack()
				}
				checkinButton.setOnClickListener {
					crowdNotifierViewModel.checkInState =
						CheckInState(false, venueInfo, System.currentTimeMillis(), System.currentTimeMillis(), ReminderOption.OFF)
					showCheckInFragment()
				}
			}
		}.root
	}

	private fun generatePdf(isPrint: Boolean) {
		val pdf = qrCodeViewModel.selectedQrCodePdf.value
		if (pdf == null) {
			qrCodeViewModel.selectedQrCodePdf.observe(viewLifecycleOwner) {
				if (isPrint) printPdf(it) else sharePdf(it)
			}
			qrCodeViewModel.createQrCodePdf()
		} else {
			if (isPrint) printPdf(pdf) else sharePdf(pdf)
		}
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
				startActivity(this)
			}
		}
	}

	private fun showCheckInFragment() {
		requireActivity().supportFragmentManager.beginTransaction()
			.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
			.replace(R.id.main_fragment_container, CheckInFragment.newInstance())
			.addToBackStack(CheckInFragment::class.java.canonicalName)
			.commitAllowingStateLoss()
	}
}