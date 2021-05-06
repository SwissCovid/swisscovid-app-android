package ch.admin.bag.dp3t.inform

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.databinding.ItemSelectableCheckinBinding
import ch.admin.bag.dp3t.inform.models.SelectableCheckinItem

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
				checkinDetail1.text = "TODO"
				checkinDetail2.text = "TODO"
				checkbox.isChecked = selectableCheckinItem.isSelected
				checkbox.setOnCheckedChangeListener { _, isChecked ->
					selectableCheckinItem.isSelected = isChecked
					itemSelectionListener?.invoke(selectableCheckinItem.diaryEntry, isChecked)
				}
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