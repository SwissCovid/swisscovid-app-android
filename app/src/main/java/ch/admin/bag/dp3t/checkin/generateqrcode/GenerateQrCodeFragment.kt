package ch.admin.bag.dp3t.checkin.generateqrcode

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.checkin.models.VenueType
import ch.admin.bag.dp3t.databinding.FragmentGenerateQrCodeBinding
import ch.admin.bag.dp3t.extensions.getNameRes

private const val KEY_SELECTED_VENUE_TYPE = "KEY_SELECTED_VENUE_TYPE"

class GenerateQrCodeFragment : Fragment() {

	companion object {
		fun newInstance() = GenerateQrCodeFragment()
	}

	private lateinit var binding: FragmentGenerateQrCodeBinding
	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentGenerateQrCodeBinding.inflate(layoutInflater).apply {
			generateQrCodeCancel.setOnClickListener { popFragmentAndHideKeyboard() }

			//TODO: Remove these hardcoded Events
			val venueTypes = arrayListOf(VenueType.PRIVATE_EVENT, VenueType.MEETING_ROOM, VenueType.OFFICE_SPACE, VenueType.OTHER)
			val selectedVenueType =
				VenueType.forNumber(savedInstanceState?.getInt(KEY_SELECTED_VENUE_TYPE) ?: VenueType.PRIVATE_EVENT.number)
			for (venueType in venueTypes) {
				val radioButton = RadioButton(requireContext())
				radioButton.setText(venueType.getNameRes())
				radioButton.setRadioButtonColor()
				radioButton.tag = venueType.number
				generateQrCodeRadioGroup.addView(radioButton)
				if (venueType == selectedVenueType) generateQrCodeRadioGroup.check(radioButton.id)
			}
			qrCodeGenerate.setOnClickListener {
				generateQrCode(titleEditText.text.toString(), getSelectedVenueType())
			}
			qrCodeGenerate.isEnabled = !titleEditText.text.isNullOrBlank()
			titleEditText.doOnTextChanged { text, _, _, _ -> qrCodeGenerate.isEnabled = !text.isNullOrBlank() }
		}
		return binding.root
	}

	private fun generateQrCode(title: String, venueType: VenueType) {
		qrCodeViewModel.generateAndSaveQrCode(title, venueType)
		popFragmentAndHideKeyboard()
	}

	private fun popFragmentAndHideKeyboard() {
		requireActivity().supportFragmentManager.popBackStack()
		val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputMethodManager.hideSoftInputFromWindow(binding.titleEditText.windowToken, 0)
	}

	private fun getSelectedVenueType() =
		VenueType.forNumber(binding.root.findViewById<View>(binding.generateQrCodeRadioGroup.checkedRadioButtonId).tag as Int)


	override fun onSaveInstanceState(outState: Bundle) {
		outState.putInt(KEY_SELECTED_VENUE_TYPE, getSelectedVenueType().number)
		super.onSaveInstanceState(outState)
	}
}

fun RadioButton.setRadioButtonColor() {
	val colorStateListButton = ColorStateList(
		arrayOf(
			intArrayOf(-android.R.attr.state_checked),
			intArrayOf(android.R.attr.state_checked)
		), intArrayOf(
			Color.parseColor("#cdcdd0"),
			Color.parseColor("#5094bf")
		)
	)
	val colorStateListText = ColorStateList(
		arrayOf(
			intArrayOf(-android.R.attr.state_checked),
			intArrayOf(android.R.attr.state_checked)
		), intArrayOf(
			Color.parseColor("#4A4969"),
			Color.parseColor("#5094bf")
		)
	)
	buttonTintList = colorStateListButton
	setTextColor(colorStateListText)

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
		buttonTintBlendMode = BlendMode.SRC_IN
	} else {
		buttonTintMode = PorterDuff.Mode.SRC_IN
	}
}