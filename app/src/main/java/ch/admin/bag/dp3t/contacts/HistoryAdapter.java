package ch.admin.bag.dp3t.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

import org.dpppt.android.sdk.internal.history.HistoryEntry;

import ch.admin.bag.dp3t.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

	private ArrayList<HistoryEntry> items;

	public HistoryAdapter() {
		items = new ArrayList<>();
	}

	@NonNull
	@Override
	public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
		return new HistoryViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
		holder.bind(items.get(position), position);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void setItems(Collection<HistoryEntry> items) {
		this.items = new ArrayList<>(items);
		notifyDataSetChanged();
	}

	public void addItem(HistoryEntry item) {
		this.items.add(item);
		notifyItemInserted(items.size() - 1);
	}

	public void addItems(Collection<HistoryEntry> items) {
		this.items.addAll(items);
		notifyItemRangeInserted(items.size() - 1, items.size());
	}

}
