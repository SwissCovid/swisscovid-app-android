package ch.admin.bag.dp3t

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.checkin.utils.ErrorHelper
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState
import ch.admin.bag.dp3t.databinding.FragmentInstantBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import com.google.android.gms.instantapps.InstantApps
import com.google.android.gms.instantapps.PackageManagerCompat
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.model.VenueInfo
import org.crowdnotifier.android.sdk.utils.QrUtils.*
import java.nio.charset.StandardCharsets


private const val REQUEST_CODE_INSTALL = 1

class InstantFragment : Fragment() {

	companion object {
		val TAG: String = InstantFragment::class.java.canonicalName!!
		fun newInstance() = InstantFragment()
	}

	private lateinit var binding: FragmentInstantBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentInstantBinding.inflate(inflater)
		return binding.apply {
			val qrCodeUrl = requireActivity().intent?.data?.toString()
			Log.d(TAG, "QR Code Url: $qrCodeUrl")

			installButton.setOnClickListener { showInstallPrompt(qrCodeUrl) }

			showVenueInfo(qrCodeUrl)

		}.root
	}

	private fun showVenueInfo(qrCodeUrl: String?) {
		try {
			val venueInfo: VenueInfo = CrowdNotifier.getVenueInfo(qrCodeUrl, BuildConfig.ENTRY_QR_CODE_HOST)
			binding.apply {
				title.text = venueInfo.title
				subtitle.setText(venueInfo.getSubtitle())
			}
		} catch (e: QRException) {
			handleInvalidQRCodeExceptions(e, qrCodeUrl)
		}
	}

	private fun handleInvalidQRCodeExceptions(e: QRException, qrCodeUrl: String?) {
		binding.apply {
			title.setText(R.string.app_name)
			installButton.setText(R.string.playservices_install)

			// Note: https://qr.swisscovid.ch/try-now/ is defined as the default url in the Manifest. When clicking on
			// "Try now" in the Playstore the Instant App is started with this url and some parameters, e.g.:
			// https://qr.swisscovid.ch/try-now/?referrer=utm_source%3D(not%2520set)%26utm_medium%3D(not%2520set)

			// If the instant app is started without a url (should never happen) or the default url, don't show any
			// QR Code error information.
			if (qrCodeUrl == null || Uri.parse(qrCodeUrl).path?.contains("try-now") == true) return

			errorView.isVisible = true
			illu.isVisible = false
			when (e) {
				is NotYetValidException ->
					ErrorHelper.updateErrorView(errorView, CrowdNotifierErrorState.QR_CODE_NOT_YET_VALID, null, context, false)
				is NotValidAnymoreException ->
					ErrorHelper.updateErrorView(errorView, CrowdNotifierErrorState.QR_CODE_NOT_VALID_ANYMORE, null, context, false)
				else -> ErrorHelper.updateErrorView(errorView, CrowdNotifierErrorState.NO_VALID_QR_CODE, null, context, false)
			}
		}
	}

	private fun showInstallPrompt(qrCodeUrl: String?) {
		storeInstantAppCookie(qrCodeUrl)
		val postInstallIntent = Intent(Intent.ACTION_MAIN)
			.addCategory(Intent.CATEGORY_DEFAULT)
			.setPackage(requireContext().packageName)
		InstantApps.showInstallPrompt(requireActivity(), postInstallIntent, REQUEST_CODE_INSTALL, null)
	}

	private fun storeInstantAppCookie(qrCodeUrl: String?) {

		val pmc: PackageManagerCompat = InstantApps.getPackageManagerCompat(requireContext())
		val hasValidVenueInfo =
			runCatching { CrowdNotifier.getVenueInfo(qrCodeUrl, BuildConfig.ENTRY_QR_CODE_HOST) }.getOrNull() != null
		val cookieContent: ByteArray? = if (qrCodeUrl == null || !hasValidVenueInfo) {
			null
		} else {
			qrCodeUrl.toByteArray(StandardCharsets.UTF_8)
		}
		if (cookieContent == null || cookieContent.size <= pmc.instantAppCookieMaxSize) {
			pmc.instantAppCookie = cookieContent
			Log.d(TAG, "Instant Cookie saved: $qrCodeUrl")
		} else {
			Log.d(TAG, "No instant cookie saved: $qrCodeUrl")
		}
	}

}