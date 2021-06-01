package ch.admin.bag.dp3t.checkin.diary.items;

import android.view.View;

import org.crowdnotifier.android.sdk.model.ExposureEvent;

import ch.admin.bag.dp3t.checkin.models.DiaryEntry;

public class ItemVenueVisit extends VenueVisitRecyclerItem {

	private final ExposureEvent exposure;
	private final DiaryEntry diaryEntry;
	private final View.OnClickListener onClickListener;

	public ItemVenueVisit(ExposureEvent exposure, DiaryEntry diaryEntry, View.OnClickListener onClickListener) {
		this.exposure = exposure;
		this.diaryEntry = diaryEntry;
		this.onClickListener = onClickListener;
	}

	public ExposureEvent getExposure() {
		return exposure;
	}

	public DiaryEntry getDiaryEntry() {
		return diaryEntry;
	}

	public View.OnClickListener getOnClickListener() {
		return onClickListener;
	}

	@Override
	public ViewType getViewType() {
		return ViewType.VENUE;
	}

}
