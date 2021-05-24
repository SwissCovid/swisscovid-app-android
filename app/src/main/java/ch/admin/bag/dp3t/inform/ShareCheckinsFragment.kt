package ch.admin.bag.dp3t.inform

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentShareCheckinsBinding
import ch.admin.bag.dp3t.extensions.showFragment

class ShareCheckinsFragment : TraceKeyShareBaseFragment() {

	companion object {
		@JvmStatic
		fun newInstance() = ShareCheckinsFragment()
	}

	private lateinit var binding: FragmentShareCheckinsBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		binding = FragmentShareCheckinsBinding.inflate(inflater).apply {
			(requireActivity() as InformActivity).allowBackButton(false)
			val adapter = CheckinAdapter()
			checkinsRecyclerView.adapter = adapter
			adapter.setData(informViewModel.getSelectableCheckinItems())
			adapter.itemSelectionListener { selectedItem, selected ->
				informViewModel.setDiaryItemSelected(selectedItem.id, selected)
			}
			selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
				informViewModel.getSelectableCheckinItems().forEach {
					informViewModel.setDiaryItemSelected(it.diaryEntry.id, isChecked)
				}
				adapter.setData(informViewModel.getSelectableCheckinItems())
			}
			dontSendButton.paintFlags = dontSendButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
			dontSendButton.setOnClickListener {
				if (informViewModel.hasSharedDP3TKeys) {
					upload()
				} else {
					showFragment(NotThankYouFragment.newInstance(), R.id.inform_fragment_container)
				}
			}
			sendButton.setOnClickListener {
				informViewModel.hasSharedCheckins = true
				upload()
			}
		}
		return binding.root
	}

	private fun upload() {
		performUpload(onSuccess = { showFragment(ThankYouFragment.newInstance(), R.id.inform_fragment_container) })
	}

	override fun setLoadingViewVisible(isVisible: Boolean) {
		with(binding) {
			loadingView.isVisible = isVisible
			sendButton.isEnabled = !isVisible
			dontSendButton.isEnabled = !isVisible
		}
	}

}