package ch.admin.bag.dp3t.checkin;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.checkinflow.QrCodeScannerFragment;

public class CheckinOverviewFragment extends Fragment {

	public static CheckinOverviewFragment newInstance() {
		return new CheckinOverviewFragment();
	}

	public CheckinOverviewFragment() { super(R.layout.fragment_checkin_overview); }

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.checkin_overview_scan_qr).setOnClickListener(
				v -> {
					showQRCodeScanner();
				});

		view.findViewById(R.id.checkin_overview_history).setOnClickListener(
				v -> {
					// TODO
				});

		((Toolbar) view.findViewById(R.id.checkin_overview_toolbar))
				.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());
	}

	private void showQRCodeScanner() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, QrCodeScannerFragment.newInstance())
				.addToBackStack(QrCodeScannerFragment.class.getCanonicalName())
				.commit();
	}

}
