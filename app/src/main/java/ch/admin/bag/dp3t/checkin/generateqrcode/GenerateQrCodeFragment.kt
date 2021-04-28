package ch.admin.bag.dp3t.checkin.generateqrcode

import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.databinding.FragmentGenerateQrCodeBinding

class GenerateQrCodeFragment : Fragment() {

	companion object {
		fun newInstance(): GenerateQrCodeFragment {
			return GenerateQrCodeFragment()
		}
	}

	private lateinit var binding: FragmentGenerateQrCodeBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentGenerateQrCodeBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val events = arrayListOf(EventType.PRIVATE_EVENT, EventType.MEETING_ROOM, EventType.OFFICE, EventType.OTHERS)
		for (event in events) {
			val radioButton = RadioButton(requireContext())
			radioButton.text = event.value
			radioButton.setRadioButtonColor()
			binding.generateQrCodeRadioGroup.addView(radioButton)
		}
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