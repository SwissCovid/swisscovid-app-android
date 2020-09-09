package ch.admin.bag.dp3t.stats;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;

public class StatsFragment extends Fragment {

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
	}

}
