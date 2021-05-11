package ch.admin.bag.dp3t.inform

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentShareCheckinsBinding
import ch.admin.bag.dp3t.inform.models.Status
import ch.admin.bag.dp3t.util.showFragment
import retrofit2.HttpException

class ShareCheckinsFragment : Fragment() {

	companion object {
		@JvmStatic
		fun newInstance() = ShareCheckinsFragment()
	}

	private val informViewModel: InformViewModel by activityViewModels()

	private lateinit var binding: FragmentShareCheckinsBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentShareCheckinsBinding.inflate(inflater).apply {
			informViewModel.filterSelectableDiaryItems()
			val adapter = CheckinAdapter()
			checkinsRecyclerView.adapter = adapter
			checkinsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
			adapter.setData(informViewModel.selectableCheckinItems)
			adapter.itemSelectionListener { selectedItem, selected ->
				informViewModel.selectableCheckinItems.find { it.diaryEntry == selectedItem }?.isSelected = selected
			}
			selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
				informViewModel.selectableCheckinItems.forEach { it.isSelected = isChecked }
				adapter.setData(informViewModel.selectableCheckinItems)
			}
			dontSendButton.paintFlags = dontSendButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
			dontSendButton.setOnClickListener {
				if (informViewModel.hasSharedDP3TKeys) {
					showFragment(ThankYouFragment.newInstance(), R.id.inform_fragment_container)
				} else {
					//TODO: Show "Thanks for nothing" Fragment
				}
			}
			sendButton.setOnClickListener { performUserUpload() }
		}
		return binding.root
	}

	private fun performUserUpload() {

		informViewModel.userUpload().observe(viewLifecycleOwner) {
			binding.loadingView.isVisible = it.status == Status.LOADING
			when (it.status) {
				Status.LOADING -> showLoadingView(true)
				Status.ERROR -> {
					showLoadingView(false)
					if (it.exception is HttpException) {
						showErrorDialog(InformRequestError.USER_UPLOAD_NETWORK_ERROR, it.exception.code().toString())
					} else {
						showErrorDialog(InformRequestError.USER_UPLOAD_UNKONWN_ERROR, it.exception?.message)
					}
				}
				Status.SUCCESS -> {
					showLoadingView(false)
					showFragment(ThankYouFragment.newInstance(), R.id.inform_fragment_container)
				}
			}
		}
	}

	private fun showLoadingView(isVisible: Boolean) {
		with(binding) {
			loadingView.isVisible = isVisible
			sendButton.isEnabled = !isVisible
		}
	}

	private fun showErrorDialog(error: InformRequestError, addErrorCode: String? = null) {
		val errorDialogBuilder = AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(error.errorMessage)
			.setPositiveButton(R.string.android_button_ok) { _, _ -> }
		val errorCode = error.getErrorCode(addErrorCode)
		val errorCodeView = layoutInflater.inflate(R.layout.view_dialog_error_code, view as ViewGroup?, false) as TextView
		errorCodeView.text = errorCode
		errorDialogBuilder.setView(errorCodeView)
		errorDialogBuilder.show()
	}

}