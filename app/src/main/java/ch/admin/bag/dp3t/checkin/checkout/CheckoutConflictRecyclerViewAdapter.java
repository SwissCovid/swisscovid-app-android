package ch.admin.bag.dp3t.checkin.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.models.DiaryEntry;
import ch.admin.bag.dp3t.util.StringUtil;

public class CheckoutConflictRecyclerViewAdapter
		extends RecyclerView.Adapter<CheckoutConflictRecyclerViewAdapter.ConflictingVenueVisitViewHolder> {

	private final List<ConflictingVenueVisitItem> diaryItems = new ArrayList<>();

	private ConflictingVenueVisitItem getItem(int position) {
		return diaryItems.get(position);
	}

	@NonNull
	@Override
	public ConflictingVenueVisitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ConflictingVenueVisitViewHolder(
				LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_conflicting_venue_visit, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ConflictingVenueVisitViewHolder holder, int position) {
		holder.bind(getItem(position));
	}

	@Override
	public int getItemCount() {
		return diaryItems.size();
	}

	public void setData(List<ConflictingVenueVisitItem> items) {
		diaryItems.clear();
		diaryItems.addAll(items);
		notifyDataSetChanged();
	}


	public static class ConflictingVenueVisitViewHolder extends RecyclerView.ViewHolder {

		private final TextView timeTextView;
		private final TextView nameTextView;
		private final View nameEditButton;

		public ConflictingVenueVisitViewHolder(View itemView) {
			super(itemView);
			this.timeTextView = itemView.findViewById(R.id.item_conflicting_entry_time);
			this.nameTextView = itemView.findViewById(R.id.item_conflicting_entry_name);
			this.nameEditButton = itemView.findViewById(R.id.item_conflicting_entry_edit);
		}

		public void bind(ConflictingVenueVisitItem item) {
			VenueInfo venueInfo = item.getDiaryEntry().getVenueInfo();
			nameTextView.setText(venueInfo.getTitle());
			String start = StringUtil.getHourMinuteTimeString(item.getDiaryEntry().getCheckInTime(), ":");
			String end = StringUtil.getHourMinuteTimeString(item.getDiaryEntry().getCheckOutTime(), ":");
			timeTextView.setText(start + " – " + end);
			nameEditButton.setOnClickListener(item.getOnClickListener());
		}

	}


	public static class ConflictingVenueVisitItem {

		private final DiaryEntry diaryEntry;
		private final View.OnClickListener onClickListener;

		public ConflictingVenueVisitItem(DiaryEntry diaryEntry, View.OnClickListener onClickListener) {
			this.diaryEntry = diaryEntry;
			this.onClickListener = onClickListener;
		}

		public DiaryEntry getDiaryEntry() {
			return diaryEntry;
		}

		public View.OnClickListener getOnClickListener() {
			return onClickListener;
		}

	}

}
