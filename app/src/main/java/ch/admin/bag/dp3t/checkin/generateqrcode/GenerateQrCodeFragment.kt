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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import ch.admin.bag.dp3t.databinding.FragmentGenerateQrCodeBinding
import org.crowdnotifier.android.sdk.model.v3.ProtoV3

class GenerateQrCodeFragment : Fragment() {

	companion object {
		fun newInstance(): GenerateQrCodeFragment {
			return GenerateQrCodeFragment()
		}
	}

	private lateinit var binding: FragmentGenerateQrCodeBinding
	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentGenerateQrCodeBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.generateQrCodeCancel.setOnClickListener {
			requireActivity().supportFragmentManager.popBackStack()
		}

		val events = arrayListOf(EventType.PRIVATE_EVENT, EventType.MEETING_ROOM, EventType.OFFICE, EventType.OTHERS)
		for (event in events) {
			val radioButton = RadioButton(requireContext())
			radioButton.text = event.value
			radioButton.setRadioButtonColor()
			binding.generateQrCodeRadioGroup.addView(radioButton)
		}

		binding.qrCodeGenerate.setOnClickListener {
			val trac = ProtoV3.TraceLocation.newBuilder().setDescription("mau").build()
			val protobuff: ProtoV3.QRCodePayload =
				ProtoV3.QRCodePayload.newBuilder().setVersion(3)
					.setLocationData(trac).build()
			//TODO PP-966  for saving a list, we need a superior class which contains an list of QRCodePayloads
			//so we can use this approach https://stackoverflow.com/questions/64430872/how-to-save-a-list-of-objects-with-proto-datastore
			qrCodeViewModel.saveQRCodePayload(protobuff)
		}

		qrCodeViewModel.qrCodeStateLiveData.observe(viewLifecycleOwner, Observer { state ->
			when (state) {
				is QrCodePayloadState.SUCCESS -> {
					requireActivity().supportFragmentManager.popBackStack()
				}
				is QrCodePayloadState.ERROR -> {
					//TODO PP-966 show error
				}
			}
		})
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