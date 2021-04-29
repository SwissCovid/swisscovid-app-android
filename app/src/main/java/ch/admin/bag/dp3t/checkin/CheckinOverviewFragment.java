package ch.admin.bag.dp3t.checkin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.checkinflow.QrCodeScannerFragment;
import ch.admin.bag.dp3t.util.StringUtil;

public class CheckinOverviewFragment extends Fragment {

	private CrowdNotifierViewModel crowdNotifierViewModel;

	public static CheckinOverviewFragment newInstance() {
		return new CheckinOverviewFragment();
	}

	public CheckinOverviewFragment() { super(R.layout.fragment_checkin_overview); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		crowdNotifierViewModel = new ViewModelProvider(requireActivity()).get(CrowdNotifierViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

		view.findViewById(R.id.checkin_overview_history).setOnClickListener(
				v -> {
					// TODO
				});

		((Toolbar) view.findViewById(R.id.checkin_overview_toolbar))
				.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

		View checkinCard = view.findViewById(R.id.checkin_overview_scan_qr);
		View checkinView = checkinCard.findViewById(R.id.checkin_view);
		View checkoutView = checkinCard.findViewById(R.id.checkout_view);

		crowdNotifierViewModel.isCheckedIn().observe(getViewLifecycleOwner(), isCheckedIn -> {
			if (isCheckedIn) {
				checkoutView.setVisibility(View.VISIBLE);
				checkinView.setVisibility(View.GONE);
				crowdNotifierViewModel.startCheckInTimer();
			} else {
				checkoutView.setVisibility(View.GONE);
				checkinView.setVisibility(View.VISIBLE);
			}
		});

		checkinCard.findViewById(R.id.checkin_button).setOnClickListener(v -> showQrCodeScannerFragment());
		checkinCard.findViewById(R.id.checkout_button).setOnClickListener(v -> showCheckOutFragment());

		TextView checkinTime = checkinCard.findViewById(R.id.checkin_time);
		crowdNotifierViewModel.getTimeSinceCheckIn().observe(getViewLifecycleOwner(),
				duration -> checkinTime.setText(StringUtil.getShortDurationString(duration)));
	}

	private void showCheckOutFragment() {
		//TODO
	}

	private void showQrCodeScannerFragment() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, QrCodeScannerFragment.newInstance())
				.addToBackStack(QrCodeScannerFragment.class.getCanonicalName())
				.commit();
	}

}
