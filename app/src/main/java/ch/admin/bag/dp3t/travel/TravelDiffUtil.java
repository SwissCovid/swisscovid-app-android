package ch.admin.bag.dp3t.travel;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class TravelDiffUtil extends DiffUtil.Callback {

	List<TravelRecyclerItem> oldData;
	List<TravelRecyclerItem> newData;


	public TravelDiffUtil(List<TravelRecyclerItem> oldData, List<TravelRecyclerItem> newData) {
		this.oldData = oldData;
		this.newData = newData;
	}

	@Override
	public int getOldListSize() {
		return oldData.size();
	}

	@Override
	public int getNewListSize() {
		return newData.size();
	}

	@Override
	public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
		return oldData.get(oldItemPosition).hashCode() == newData.get(newItemPosition).hashCode();
	}

	@Override
	public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
		boolean same = oldData.get(oldItemPosition).equals(newData.get(newItemPosition));
		return same;
	}

}
