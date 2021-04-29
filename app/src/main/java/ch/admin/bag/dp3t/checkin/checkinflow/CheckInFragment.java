package ch.admin.bag.dp3t.checkin.checkinflow;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel;
import ch.admin.bag.dp3t.checkin.models.ReminderOption;
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper;
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper;
import ch.admin.bag.dp3t.checkin.utils.VenueInfoExtensions;

public class CheckInFragment extends Fragment {

	public static final String TAG = CheckInFragment.class.getCanonicalName();

	private CrowdNotifierViewModel viewModel;
	private VenueInfo venueInfo;

	public CheckInFragment() { super(R.layout.fragment_check_in); }

	public static CheckInFragment newInstance() {
		return new CheckInFragment();
	}


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(CrowdNotifierViewModel.class);
		if (viewModel.getCheckInState() != null) {
			venueInfo = viewModel.getCheckInState().getVenueInfo();
		}
		checkIfAutoCheckoutHappened();
	}

	@Override
	public void onStart() {
		super.onStart();
		checkIfAutoCheckoutHappened();
	}

	private void checkIfAutoCheckoutHappened() {
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		TextView titleTextView = view.findViewById(R.id.check_in_fragment_title);
		TextView subtitleTextView = view.findViewById(R.id.check_in_fragment_subtitle);
		ImageView venueTypeIcon = view.findViewById(R.id.check_in_fragment_venue_type_icon);
		View checkInButton = view.findViewById(R.id.check_in_fragment_check_in_button);
		Toolbar toolbar = view.findViewById(R.id.check_in_fragment_toolbar);
		MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.check_in_fragment_toggle_group);

		titleTextView.setText(venueInfo.getTitle());
		subtitleTextView.setText(VenueInfoExtensions.getSubtitle(venueInfo));
		venueTypeIcon.setImageResource(VenueInfoExtensions.getVenueTypeDrawable(venueInfo));

		checkInButton.setOnClickListener(v -> {
			long checkInTime = System.currentTimeMillis();
			viewModel.startCheckInTimer();
			viewModel.setCheckedIn(true);
			viewModel.getCheckInState().setCheckInTime(checkInTime);
			NotificationHelper.getInstance(getContext()).startOngoingNotification(checkInTime, venueInfo);
			CrowdNotifierReminderHelper.set8HourReminder(checkInTime, getContext());
			CrowdNotifierReminderHelper.setAutoCheckOut(checkInTime, getContext());
			CrowdNotifierReminderHelper
					.setReminder(checkInTime + viewModel.getSelectedReminderOption().getDelayMillis(), getContext());
			popBackToHomeFragment();
		});

		for (ReminderOption option : ReminderOption.values()) {
			((MaterialButton) view.findViewById(option.getToggleButtonId())).setText(option.getName(getContext()));
		}
		toggleGroup.check(viewModel.getSelectedReminderOption().getToggleButtonId());
		toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
			if (isChecked) {
				ReminderOption selectedReminderOption = ReminderOption.getReminderOptionForToggleButtonId(checkedId);
				viewModel.setSelectedReminderOption(selectedReminderOption);
			}
		});

		toolbar.setNavigationOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

		super.onViewCreated(view, savedInstanceState);
	}

	private void popBackToHomeFragment() {
		requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

}
