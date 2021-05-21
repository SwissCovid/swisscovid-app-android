package ch.admin.bag.dp3t.inform

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.databinding.ItemSelectableCheckinBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import ch.admin.bag.dp3t.inform.models.SelectableCheckinItem
import ch.admin.bag.dp3t.util.StringUtil

class CheckinAdapter : RecyclerView.Adapter<CheckinAdapter.CheckinViewHolder>() {

	private var checkins = mutableListOf<SelectableCheckinItem>()
	private var itemSelectionListener: ((selectedItem: DiaryEntry, selected: Boolean) -> Unit)? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckinViewHolder {
		return CheckinViewHolder(ItemSelectableCheckinBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}

	override fun onBindViewHolder(holderCheckin: CheckinViewHolder, position: Int) {
		holderCheckin.bind(checkins[position])
	}

	override fun getItemCount() = checkins.size

	inner class CheckinViewHolder(private val binding: ItemSelectableCheckinBinding) : RecyclerView.ViewHolder(binding.root) {
		fun bind(selectableCheckinItem: SelectableCheckinItem) {
			binding.apply {
				checkinTitle.text = selectableCheckinItem.diaryEntry.venueInfo.title
				checkinDetail1.setText(selectableCheckinItem.diaryEntry.venueInfo.getSubtitle())
				checkinDetail2.text =
					StringUtil.getReportDateString(selectableCheckinItem.diaryEntry.departureTime, true, true, root.context)
				checkbox.isChecked = selectableCheckinItem.isSelected
				setStroke(selectableCheckinItem.isSelected)

				checkbox.setOnCheckedChangeListener { _, isChecked ->
					selectableCheckinItem.isSelected = isChecked
					setStroke(isChecked)
					itemSelectionListener?.invoke(selectableCheckinItem.diaryEntry, isChecked)
				}
				root.setOnClickListener { checkbox.isChecked = !checkbox.isChecked }
			}
		}

		private fun setStroke(isSelected: Boolean) {
			binding.apply {
				if (isSelected) {
					root.strokeWidth = root.context.resources.getDimensionPixelSize(R.dimen.stroke_width_default)
				} else {
					root.strokeWidth = 0
				}
				root.requestLayout()
			}
		}
	}

	fun setData(selectableCheckinItems: List<SelectableCheckinItem>) {
		checkins.clear()
		checkins.addAll(selectableCheckinItems)
		notifyDataSetChanged()
	}

	fun itemSelectionListener(listener: (selectedItem: DiaryEntry, selected: Boolean) -> Unit) {
		itemSelectionListener = listener
	}

}