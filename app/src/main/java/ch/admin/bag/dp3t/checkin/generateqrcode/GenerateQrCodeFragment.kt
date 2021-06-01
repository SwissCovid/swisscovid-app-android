package ch.admin.bag.dp3t.checkin.generateqrcode

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ch.admin.bag.dp3t.databinding.FragmentGenerateQrCodeBinding
import ch.admin.bag.dp3t.extensions.showFragment

class GenerateQrCodeFragment : Fragment() {

	companion object {
		fun newInstance() = GenerateQrCodeFragment()
	}

	private lateinit var binding: FragmentGenerateQrCodeBinding
	private val qrCodeViewModel: QRCodeViewModel by activityViewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentGenerateQrCodeBinding.inflate(layoutInflater).apply {
			generateQrCodeCancel.setOnClickListener { popFragmentAndHideKeyboard() }

			qrCodeGenerate.setOnClickListener { generateQrCode(titleEditText.text.toString()) }
			qrCodeGenerate.isEnabled = !titleEditText.text.isNullOrBlank()
			titleEditText.doOnTextChanged { text, _, _, _ -> qrCodeGenerate.isEnabled = !text.isNullOrBlank() }
		}
		return binding.root
	}

	private fun generateQrCode(title: String) {
		qrCodeViewModel.generateAndSaveQrCode(title).observe(viewLifecycleOwner) {
			requireActivity().supportFragmentManager.popBackStack()
			showFragment(QrCodeFragment.newInstance(it))
		}
	}

	private fun popFragmentAndHideKeyboard() {
		requireActivity().supportFragmentManager.popBackStack()
		val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputMethodManager.hideSoftInputFromWindow(binding.titleEditText.windowToken, 0)
	}

}