package ch.admin.bag.dp3t.checkin;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;

public class CheckinOverviewFragment extends Fragment {

	public static CheckinOverviewFragment newInstance() {
		return new CheckinOverviewFragment();
	}

	public CheckinOverviewFragment() { super(R.layout.fragment_checkin_overview); }

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.checkin_overview_scan_qr).setOnClickListener(
				v -> {
					// TODO
				});

		view.findViewById(R.id.checkin_overview_history).setOnClickListener(
				v -> {
					// TODO
				});
	}

}
