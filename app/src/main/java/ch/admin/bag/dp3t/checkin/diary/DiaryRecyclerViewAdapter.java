package ch.admin.bag.dp3t.checkin.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisit;
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisitDayHeader;
import ch.admin.bag.dp3t.checkin.diary.items.VenueVisitRecyclerItem;
import ch.admin.bag.dp3t.extensions.CommonVenueInfoExtensionsKt;
import ch.admin.bag.dp3t.util.StringUtil;

public class DiaryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final List<VenueVisitRecyclerItem> items = new ArrayList<>();

	@Override
	public int getItemViewType(int position) { return items.get(position).getViewType().ordinal(); }

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		VenueVisitRecyclerItem.ViewType type = VenueVisitRecyclerItem.ViewType.values()[viewType];

		switch (type) {
			case DAY_HEADER:
				return new DayHeaderViewHolder(
						LayoutInflater.from(parent.getContext())
								.inflate(R.layout.item_checkin_venue_visits_day_header, parent, false));
			case VENUE:
				return new VenueVisitViewHolder(
						LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkin_venue_visit, parent, false));
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		VenueVisitRecyclerItem item = items.get(position);

		switch (item.getViewType()) {
			case DAY_HEADER:
				((DayHeaderViewHolder) holder).bind((ItemVenueVisitDayHeader) item);
				break;
			case VENUE:
				((VenueVisitViewHolder) holder).bind((ItemVenueVisit) item);
				break;
		}
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setData(List<VenueVisitRecyclerItem> items) {
		this.items.clear();
		this.items.addAll(items);
		notifyDataSetChanged();
	}


	public static class DayHeaderViewHolder extends RecyclerView.ViewHolder {

		private final TextView dayLabel;

		public DayHeaderViewHolder(View itemView) {
			super(itemView);
			dayLabel = itemView.findViewById(R.id.item_diary_day_header_text_view);
		}

		public void bind(ItemVenueVisitDayHeader item) {
			dayLabel.setText(item.getDayLabel());
		}

	}


	public static class VenueVisitViewHolder extends RecyclerView.ViewHolder {

		private final TextView timeTextView;
		private final TextView nameTextView;
		private final TextView locationTextView;
		private final ImageView statusIcon;

		public VenueVisitViewHolder(View itemView) {
			super(itemView);
			this.timeTextView = itemView.findViewById(R.id.item_diary_entry_time);
			this.nameTextView = itemView.findViewById(R.id.item_diary_entry_name);
			this.locationTextView = itemView.findViewById(R.id.item_diary_entry_location);
			this.statusIcon = itemView.findViewById(R.id.item_diary_entry_status_icon);
		}

		public void bind(ItemVenueVisit item) {
			VenueInfo venueInfo = item.getDiaryEntry().getVenueInfo();
			nameTextView.setText(venueInfo.getTitle());
			locationTextView.setText(CommonVenueInfoExtensionsKt.getSubtitle(venueInfo));
			String start = StringUtil.getHourMinuteTimeString(item.getDiaryEntry().getArrivalTime(), ":");
			String end = StringUtil.getHourMinuteTimeString(item.getDiaryEntry().getDepartureTime(), ":");
			timeTextView.setText(start + " — " + end);
			if (item.getExposure() == null) {
				statusIcon.setVisibility(View.GONE);
			} else {
				statusIcon.setVisibility(View.VISIBLE);
			}
			itemView.setOnClickListener(item.getOnClickListener());
		}

	}

}
