package ch.admin.bag.dp3t.checkin.checkinflow;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

import org.crowdnotifier.android.sdk.CrowdNotifier;
import org.crowdnotifier.android.sdk.model.VenueInfo;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel;
import ch.admin.bag.dp3t.checkin.models.CheckInState;
import ch.admin.bag.dp3t.checkin.models.DiaryEntry;
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage;
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper;
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper;
import ch.admin.bag.dp3t.extensions.VenueInfoExtensionsKt;
import ch.admin.bag.dp3t.util.StringUtil;

public class CheckOutFragment extends Fragment {

	public static final String TAG = CheckOutFragment.class.getCanonicalName();

	private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

	private CrowdNotifierViewModel viewModel;
	private VenueInfo venueInfo;
	private CheckInState checkInState;

	private TextView titleTextView;
	private TextView subtitleTextView;
	private View doneButton;
	private View cancelButton;
	private TextView fromTime;
	private TextView toTime;
	private TextView dateTextView;
	private Button hideInDiaryButton;

	public CheckOutFragment() { super(R.layout.fragment_check_out_and_edit); }

	public static CheckOutFragment newInstance() {
		return new CheckOutFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(CrowdNotifierViewModel.class);
		checkInState = viewModel.getCheckInState();
		if (checkInState != null) {
			venueInfo = checkInState.getVenueInfo();
		}
		checkIfAutoCheckoutHappened();
	}

	@Override
	public void onStart() {
		super.onStart();
		checkIfAutoCheckoutHappened();
	}

	private void checkIfAutoCheckoutHappened() {
		if (viewModel.getCheckInState() == null) {
			popBackToHomeFragment();
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		titleTextView = view.findViewById(R.id.check_out_fragment_title);
		subtitleTextView = view.findViewById(R.id.check_out_fragment_subtitle);
		doneButton = view.findViewById(R.id.check_out_fragment_done_button);
		cancelButton = view.findViewById(R.id.check_out_fragment_cancel_button);
		fromTime = view.findViewById(R.id.check_out_fragment_from_text_view);
		toTime = view.findViewById(R.id.check_out_fragment_to_text_view);
		dateTextView = view.findViewById(R.id.check_out_fragment_date);
		hideInDiaryButton = view.findViewById(R.id.edit_diary_entry_hide_from_diary_button);

		titleTextView.setText(venueInfo.getTitle());
		subtitleTextView.setText(VenueInfoExtensionsKt.getSubtitle(venueInfo));

		checkInState.setCheckOutTime(System.currentTimeMillis());
		refreshTimeTextViews();

		fromTime.setOnClickListener(v -> showTimePicker(true));
		toTime.setOnClickListener(v -> showTimePicker(false));

		hideInDiaryButton.setVisibility(View.GONE);

		doneButton.setOnClickListener(v -> {
			CrowdNotifierReminderHelper.removeAllReminders(getContext());
			saveEntry();
			NotificationHelper notificationHelper = NotificationHelper.getInstance(getContext());
			notificationHelper.stopOngoingNotification();
			notificationHelper.removeReminderNotification();
			popBackToHomeFragment();
		});
		cancelButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
	}

	private void refreshTimeTextViews() {
		fromTime.setText(StringUtil.getHourMinuteTimeString(checkInState.getCheckInTime(), "  :  "));
		toTime.setText(StringUtil.getHourMinuteTimeString(checkInState.getCheckOutTime(), "  :  "));
		dateTextView.setText(StringUtil.getCheckOutDateString(getContext(), checkInState.getCheckInTime(),
				checkInState.getCheckOutTime()));
	}

	private void showTimePicker(boolean isFromTime) {
		Calendar time = Calendar.getInstance();
		if (isFromTime) {
			time.setTimeInMillis(checkInState.getCheckInTime());
		} else {
			time.setTimeInMillis(checkInState.getCheckOutTime());
		}
		int hour = time.get(Calendar.HOUR_OF_DAY);
		int minute = time.get(Calendar.MINUTE);
		TimePickerDialog timePicker;
		timePicker = new TimePickerDialog(getContext(), (picker, selectedHour, selectedMinute) -> {
			time.set(Calendar.HOUR_OF_DAY, selectedHour);
			time.set(Calendar.MINUTE, selectedMinute);
			if (isFromTime) {
				checkInState.setCheckInTime(time.getTimeInMillis());
			} else {
				checkInState.setCheckOutTime(time.getTimeInMillis());
			}
			if (checkInState.getCheckOutTime() < checkInState.getCheckInTime()) {
				checkInState.setCheckOutTime(checkInState.getCheckOutTime() + ONE_DAY_IN_MILLIS);
			} else if (checkInState.getCheckInTime() + ONE_DAY_IN_MILLIS < checkInState.getCheckOutTime()) {
				checkInState.setCheckOutTime(checkInState.getCheckOutTime() - ONE_DAY_IN_MILLIS);
			}
			refreshTimeTextViews();
		}, hour, minute, true);

		timePicker.show();
	}

	private void saveEntry() {
		long checkIn = checkInState.getCheckInTime();
		long checkOut = checkInState.getCheckOutTime();
		long id = CrowdNotifier.addCheckIn(checkIn, checkOut, venueInfo, getContext());
		DiaryStorage.getInstance(getContext()).addEntry(new DiaryEntry(id, checkIn, checkOut, venueInfo));
		viewModel.setCheckInState(null);
	}

	private void popBackToHomeFragment() {
		requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

}
