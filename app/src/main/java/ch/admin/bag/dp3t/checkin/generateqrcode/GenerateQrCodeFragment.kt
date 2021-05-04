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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.checkin.models.VenueType
import ch.admin.bag.dp3t.databinding.FragmentGenerateQrCodeBinding


class GenerateQrCodeFragment : Fragment() {

	companion object {
		fun newInstance() = GenerateQrCodeFragment()
	}

	private lateinit var binding: FragmentGenerateQrCodeBinding
	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentGenerateQrCodeBinding.inflate(layoutInflater).apply {
			generateQrCodeCancel.setOnClickListener { cancel() }

			//TODO: Remove these hardcoded Events
			val events = arrayListOf(EventType.PRIVATE_EVENT, EventType.MEETING_ROOM, EventType.OFFICE, EventType.OTHERS)
			for (event in events) {
				val radioButton = RadioButton(requireContext())
				radioButton.text = event.value
				radioButton.setRadioButtonColor()
				generateQrCodeRadioGroup.addView(radioButton)
			}
			qrCodeGenerate.setOnClickListener {
				//TODO: Set correct Venue Type
				generateQrCode(titleEditText.text.toString(), VenueType.CAFETERIA)
			}

		}
		return binding.root
	}

	private fun generateQrCode(title: String, venueType: VenueType) {
		qrCodeViewModel.generateAndSaveQrCode(title, venueType)
		cancel()
	}

	private fun cancel() {
		requireActivity().supportFragmentManager.popBackStack()
		val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputMethodManager.hideSoftInputFromWindow(binding.titleEditText.windowToken, 0)
	}

	enum class EventType(val value: String) {
		PRIVATE_EVENT("Privater Event"),
		MEETING_ROOM("Sitzungsraum"),
		OFFICE("Büroräume"),
		OTHERS("Andere")
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