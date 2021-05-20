package ch.admin.bag.dp3t.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.databinding.*
import ch.admin.bag.dp3t.extensions.getDetailsString
import ch.admin.bag.dp3t.util.StringUtil
import org.crowdnotifier.android.sdk.model.ExposureEvent
import org.dpppt.android.sdk.models.ExposureDay
import java.util.*
import kotlin.collections.ArrayList

class ReportsRecyclerAdapter : RecyclerView.Adapter<ReportsRecyclerAdapter.ReportViewHolder>() {

	private var items: List<ReportItem> = emptyList()
	private var onClickListener: ((ReportItem) -> Unit)? = null

	fun setItems(newItems: List<ReportItem>) {
		this.items = ArrayList<ReportItem>().apply { addAll(newItems) }
		notifyDataSetChanged()
	}


	fun setOnClickListener(listener: (ReportItem) -> Unit) {
		this.onClickListener = listener
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
		ReportViewHolder(ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))

	override fun onBindViewHolder(holder: ReportViewHolder, position: Int) = holder.bind(items[position])

	override fun getItemCount() = items.size


	/*--------VIEW HOLDERS-------*/


	inner class ReportViewHolder(private val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root) {
		fun bind(item: ReportItem) {
			binding.apply {
				cardView.setOnClickListener { onClickListener?.invoke(item) }
				place.isVisible = item is CheckinReportItem && item.diaryEntry != null
				when (item) {
					is ProximityTracingReportItem -> {
						reportType.setText(R.string.meldung_detail_exposed_list_card_title_encounters)
						reportDetails.text = item.exposures.joinToString("\n") {
							StringUtil.getReportDateString(
								it.exposedDate.getStartOfDay(TimeZone.getDefault()),
								true,
								false,
								root.context
							)
						}
					}
					is CheckinReportItem -> {
						reportType.setText(R.string.meldung_detail_exposed_list_card_title_checkin)
						place.text = item.diaryEntry?.venueInfo?.title
						reportDetails.text = item.exposure.getDetailsString(root.context)
					}
				}
			}
		}
	}

}

/*--------ITEMS-------*/

abstract class ReportItem {
	companion object {
		const val TYPE_PROXIMITY_TRACING_REPORT = 0
		const val TYPE_CHECKIN_REPORT = 1

	}

	abstract val type: Int
}

class ProximityTracingReportItem(val exposures: List<ExposureDay>) : ReportItem() {
	override val type = TYPE_PROXIMITY_TRACING_REPORT
}

class CheckinReportItem(val exposure: ExposureEvent, val diaryEntry: DiaryEntry?) : ReportItem() {
	override val type = TYPE_CHECKIN_REPORT
}



