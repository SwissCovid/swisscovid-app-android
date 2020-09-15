package ch.admin.bag.dp3t.stats;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.models.StatsResponseModel;
import ch.admin.bag.dp3t.util.UiUtils;
import ch.admin.bag.dp3t.viewmodel.StatsViewModel;

public class StatsFragment extends Fragment {

	private StatsViewModel statsViewModel;

	private ScrollView scrollView;
	private ImageView headerView;
	private TextView totalActiveusers;
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
		statsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		scrollView = view.findViewById(R.id.stats_scroll_view);
		headerView = view.findViewById(R.id.header_view);
		totalActiveusers = view.findViewById(R.id.stats_total_active_users);
		shareAppButton = view.findViewById(R.id.share_app_button);

		setupScrollBehavior();
		setupShareAppButton();

		statsViewModel.getStatsLiveData().observe(getViewLifecycleOwner(), this::displayStats);
		statsViewModel.loadStats();
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
			String message = v.getContext().getResources().getString(R.string.share_app_message) + "\n" +
					v.getContext().getResources().getString(R.string.share_app_url);

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, message);
			intent.setType("text/pain");

			Intent shareIntent = Intent.createChooser(intent, null);
			startActivity(shareIntent);
		});
	}

	private void displayStats(StatsResponseModel stats) {
		if (stats == null) {
			// TODO(PP-753) Decide what to show, whether to show cached version or some placeholder
			return;
		}
		String text = totalActiveusers.getContext().getResources().getString(R.string.stats_counter);
		text = text.replace("{COUNT}", stats.getTotalActiveUsersInMillions());
		totalActiveusers.setText(text);
	}

}
