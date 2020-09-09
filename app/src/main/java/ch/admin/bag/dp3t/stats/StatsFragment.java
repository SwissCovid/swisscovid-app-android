package ch.admin.bag.dp3t.stats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.UiUtils;

public class StatsFragment extends Fragment {

	private ScrollView scrollView;
	private ImageView headerView;
	private Button shareAppButton;

	public static StatsFragment newInstance() {
		return new StatsFragment();
	}

	private StatsFragment() {
		super(R.layout.fragment_stats);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		scrollView = view.findViewById(R.id.stats_scroll_view);
		headerView = view.findViewById(R.id.header_view);
		shareAppButton = view.findViewById(R.id.share_app_button);

		setupScrollBehavior();
		setupShareAppButton();
	}

	private void setupScrollBehavior() {
		int scrollRangePx = getResources().getDimensionPixelSize(R.dimen.top_item_padding);
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);

		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = UiUtils.computeScrollAnimProgress(scrollY, scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = UiUtils.computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
	}

	private void setupShareAppButton() {
		shareAppButton.setOnClickListener(v -> {
			String message = v.getContext().getResources().getString(R.string.share_app_message);

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, message);
			intent.setType("text/pain");

			Intent shareIntent = Intent.createChooser(intent, null);
			startActivity(shareIntent);
		});
	}

}
