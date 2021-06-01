package ch.admin.bag.dp3t.checkin.generateqrcode

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ch.admin.bag.dp3t.checkin.generateqrcode.EventOverviewItem.Companion.TYPE_EVENT
import ch.admin.bag.dp3t.checkin.generateqrcode.EventOverviewItem.Companion.TYPE_EXPLANATION
import ch.admin.bag.dp3t.checkin.generateqrcode.EventOverviewItem.Companion.TYPE_FOOTER
import ch.admin.bag.dp3t.checkin.generateqrcode.EventOverviewItem.Companion.TYPE_GENERATE_QR_CODE_BUTTON
import ch.admin.bag.dp3t.databinding.ItemEventsExplanationBinding
import ch.admin.bag.dp3t.databinding.ItemEventsFooterBinding
import ch.admin.bag.dp3t.databinding.ItemGenerateQrCodeBinding
import ch.admin.bag.dp3t.databinding.ItemQrCodeBinding
import ch.admin.bag.dp3t.extensions.getSubtitle
import org.crowdnotifier.android.sdk.model.VenueInfo

class QrCodeAdapter(private val onClickListener: OnClickListener) : RecyclerView.Adapter<QrCodeAdapter.QrCodeBaseViewHolder>() {

	private var items: List<EventOverviewItem> = emptyList()

	fun setItems(newItems: List<EventOverviewItem>) {
		this.items = ArrayList<EventOverviewItem>().apply { addAll(newItems) }
		notifyDataSetChanged()
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrCodeBaseViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			TYPE_GENERATE_QR_CODE_BUTTON -> GenerateQrCodeViewHolder(ItemGenerateQrCodeBinding.inflate(inflater, parent, false))
			TYPE_EVENT -> EventViewHolder(ItemQrCodeBinding.inflate(inflater, parent, false))
			TYPE_EXPLANATION -> ExplanationViewHolder(ItemEventsExplanationBinding.inflate(inflater, parent, false))
			TYPE_FOOTER -> SimpleViewHolder(ItemEventsFooterBinding.inflate(inflater, parent, false))
			else -> throw IllegalArgumentException("invalid view type")
		}
	}

	override fun onBindViewHolder(holder: QrCodeBaseViewHolder, position: Int) {
		holder.bind(items[position])
	}

	override fun getItemViewType(position: Int) = items[position].type

	override fun getItemCount() = items.size


	/*--------VIEW HOLDERS-------*/

	abstract class QrCodeBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		abstract fun bind(item: EventOverviewItem)
	}

	inner class GenerateQrCodeViewHolder(private val binding: ItemGenerateQrCodeBinding) : QrCodeBaseViewHolder(binding.root) {
		override fun bind(item: EventOverviewItem) {
			binding.qrCodeGenerate.setOnClickListener { onClickListener.generateQrCode() }
		}

	}

	inner class EventViewHolder(private val binding: ItemQrCodeBinding) : QrCodeBaseViewHolder(binding.root) {
		override fun bind(item: EventOverviewItem) {
			item as EventItem
			binding.apply {
				qrCodeName.text = item.venueInfo.title
				root.setOnClickListener { onClickListener.onQrCodeClicked(item.venueInfo) }
			}
		}
	}

	inner class ExplanationViewHolder(private val binding: ItemEventsExplanationBinding) : QrCodeBaseViewHolder(binding.root) {
		override fun bind(item: EventOverviewItem) {
			item as ExplanationItem
			binding.apply {
				illu.isVisible = !item.showOnlyInfobox
				title.isVisible = !item.showOnlyInfobox
				subtitle.isVisible = !item.showOnlyInfobox
				heading.isVisible = !item.showOnlyInfobox
				generateQrCodeButton.isVisible = !item.showOnlyInfobox
				generateQrCodeButton.setOnClickListener { onClickListener.generateQrCode() }
			}
		}
	}

	inner class SimpleViewHolder(binding: ViewBinding) : QrCodeBaseViewHolder(binding.root) {
		override fun bind(item: EventOverviewItem) {}
	}
}

interface OnClickListener {
	fun generateQrCode()
	fun onQrCodeClicked(qrCodeItem: VenueInfo)
}


