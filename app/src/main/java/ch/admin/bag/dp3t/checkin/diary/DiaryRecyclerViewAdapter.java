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
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisitCurrent;
import ch.admin.bag.dp3t.checkin.diary.items.ItemVenueVisitDayHeader;
import ch.admin.bag.dp3t.checkin.diary.items.VenueVisitRecyclerItem;
import ch.admin.bag.dp3t.checkin.models.CheckInState;
import ch.admin.bag.dp3t.extensions.CommonVenueInfoExtensionsKt;
import ch.admin.bag.dp3t.util.StringUtil;

public class DiaryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final List<VenueVisitRecyclerItem> currentCheckinItems = new ArrayList<>();
	private final List<VenueVisitRecyclerItem> diaryItems = new ArrayList<>();
	private Runnable onCheckoutListener;

	private VenueVisitRecyclerItem getItem(int position) {
		if (position < currentCheckinItems.size()) {
			return currentCheckinItems.get(position);
		} else {
			return diaryItems.get(position - currentCheckinItems.size());
		}
	}

	@Override
	public int getItemViewType(int position) { return getItem(position).getViewType().ordinal(); }

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
			case CURRENT:
				return new VenueVisitCurrentViewHolder(
						LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkin_venue_visit_current, parent, false));
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		VenueVisitRecyclerItem item = getItem(position);
		switch (item.getViewType()) {
			case DAY_HEADER:
				((DayHeaderViewHolder) holder).bind((ItemVenueVisitDayHeader) item);
				break;
			case VENUE:
				((VenueVisitViewHolder) holder).bind((ItemVenueVisit) item);
				break;
			case CURRENT:
				((VenueVisitCurrentViewHolder) holder).bind((ItemVenueVisitCurrent) item);
				break;
		}
	}

	@Override
	public int getItemCount() {
		return currentCheckinItems.size() + diaryItems.size();
	}

	public void setCurrentCheckinData(ItemVenueVisitDayHeader headerItem, ItemVenueVisitCurrent checkinItem, Runnable onCheckoutListener) {
		this.onCheckoutListener = onCheckoutListener;

		boolean hadCheckin = !currentCheckinItems.isEmpty();
		if (hadCheckin) {
			notifyItemChanged(1, checkinItem);
		} else {
			currentCheckinItems.add(headerItem);
			currentCheckinItems.add(checkinItem);
			notifyItemRangeInserted(0, currentCheckinItems.size());
		}
	}

	public void updateCurrentCheckinData() {
		if (!currentCheckinItems.isEmpty()) {
			notifyItemChanged(1, currentCheckinItems.get(1));
		}
	}

	public void setCurrentCheckinDataNone() {
		int itemsToRemove = currentCheckinItems.size();
		if (itemsToRemove > 0) {
			currentCheckinItems.clear();
			notifyItemRangeRemoved(0, itemsToRemove);
		}
	}

	public void setDiaryData(List<VenueVisitRecyclerItem> items) {
		diaryItems.clear();
		diaryItems.addAll(items);
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
		private final ImageView statusIcon;

		public VenueVisitViewHolder(View itemView) {
			super(itemView);
			this.timeTextView = itemView.findViewById(R.id.item_diary_entry_time);
			this.nameTextView = itemView.findViewById(R.id.item_diary_entry_name);
			this.statusIcon = itemView.findViewById(R.id.item_diary_entry_status_icon);
		}

		public void bind(ItemVenueVisit item) {
			VenueInfo venueInfo = item.getDiaryEntry().getVenueInfo();
			nameTextView.setText(venueInfo.getTitle());
			String start = StringUtil.getHourMinuteTimeString(item.getDiaryEntry().getCheckInTime(), ":");
			String end = StringUtil.getHourMinuteTimeString(item.getDiaryEntry().getCheckOutTime(), ":");
			timeTextView.setText(start + " – " + end);
			if (item.getExposure() == null) {
				statusIcon.setVisibility(View.GONE);
			} else {
				statusIcon.setVisibility(View.VISIBLE);
			}
			itemView.setOnClickListener(item.getOnClickListener());
		}

	}


	public class VenueVisitCurrentViewHolder extends RecyclerView.ViewHolder {

		private final TextView nameTextView;
		private final TextView timeTextView;

		public VenueVisitCurrentViewHolder(View itemView) {
			super(itemView);
			nameTextView = itemView.findViewById(R.id.item_diary_current_name);
			timeTextView = itemView.findViewById(R.id.item_diary_current_time);
			itemView.findViewById(R.id.item_diary_checkout_button).setOnClickListener(v -> onCheckoutListener.run());
		}

		public void bind(ItemVenueVisitCurrent item) {
			CheckInState checkInState = item.getCheckInState();
			nameTextView.setText(checkInState.getVenueInfo().getTitle());
			long duration = System.currentTimeMillis() - checkInState.getCheckInTime();
			timeTextView.setText(StringUtil.getShortDurationString(duration));
		}

	}

}
