package ch.admin.bag.dp3t.checkin.diary;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

import org.crowdnotifier.android.sdk.CrowdNotifier;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.models.DiaryEntry;
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage;
import ch.admin.bag.dp3t.extensions.VenueInfoExtensionsKt;
import ch.admin.bag.dp3t.util.StringUtil;

public class EditDiaryEntryFragment extends Fragment {

	public static final String TAG = EditDiaryEntryFragment.class.getCanonicalName();

	private static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;
	private static final String ARG_DIARY_ENTRY_ID = "ARG_DIARY_ENTRY_ID";

	private DiaryStorage diaryStorage;
	private DiaryEntry diaryEntry;

	private TextView titleTextView;
	private TextView subtitleTextView;
	private View doneButton;
	private View cancelButton;
	private TextView fromTime;
	private TextView toTime;
	private TextView dateTextView;
	private Button actionButton;


	public EditDiaryEntryFragment() { super(R.layout.fragment_check_out_and_edit); }

	public static EditDiaryEntryFragment newInstance(long diaryEntryId) {
		EditDiaryEntryFragment fragment = new EditDiaryEntryFragment();
		Bundle args = new Bundle();
		args.putLong(ARG_DIARY_ENTRY_ID, diaryEntryId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		diaryStorage = DiaryStorage.getInstance(requireContext());
		long diaryEntryId = getArguments().getLong(ARG_DIARY_ENTRY_ID);
		diaryEntry = diaryStorage.getDiaryEntryWithId(diaryEntryId);
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
		actionButton = view.findViewById(R.id.check_out_fragment_primary_button);

		titleTextView.setText(diaryEntry.getVenueInfo().getTitle());
		subtitleTextView.setText(VenueInfoExtensionsKt.getSubtitle(diaryEntry.getVenueInfo()));

		refreshTimeTextViews();

		fromTime.setOnClickListener(v -> showTimePicker(true));
		toTime.setOnClickListener(v -> showTimePicker(false));

		doneButton.setOnClickListener(v -> {
			saveEntry();
			requireActivity().getSupportFragmentManager().popBackStack();
		});
		cancelButton.setOnClickListener(v -> {
			requireActivity().getSupportFragmentManager().popBackStack();
		});

		actionButton.setText(R.string.remove_from_diary_button);
		actionButton.setOnClickListener(v -> hideInDiary());
	}

	private void refreshTimeTextViews() {
		fromTime.setText(StringUtil.getHourMinuteTimeString(diaryEntry.getArrivalTime(), "  :  "));
		toTime.setText(StringUtil.getHourMinuteTimeString(diaryEntry.getDepartureTime(), "  :  "));
		dateTextView.setText(StringUtil.getCheckOutDateString(getContext(), diaryEntry.getArrivalTime(),
				diaryEntry.getDepartureTime()));
	}

	private void showTimePicker(boolean isFromTime) {
		Calendar time = Calendar.getInstance();
		if (isFromTime) {
			time.setTimeInMillis(diaryEntry.getArrivalTime());
		} else {
			time.setTimeInMillis(diaryEntry.getDepartureTime());
		}
		int hour = time.get(Calendar.HOUR_OF_DAY);
		int minute = time.get(Calendar.MINUTE);
		TimePickerDialog timePicker;
		timePicker = new TimePickerDialog(getContext(), (picker, selectedHour, selectedMinute) -> {
			time.set(Calendar.HOUR_OF_DAY, selectedHour);
			time.set(Calendar.MINUTE, selectedMinute);
			if (isFromTime) {
				diaryEntry.setArrivalTime(time.getTimeInMillis());
			} else {
				diaryEntry.setDepartureTime(time.getTimeInMillis());
			}
			if (diaryEntry.getDepartureTime() < diaryEntry.getArrivalTime()) {
				diaryEntry.setDepartureTime(diaryEntry.getDepartureTime() + ONE_DAY_IN_MILLIS);
			} else if (diaryEntry.getArrivalTime() + ONE_DAY_IN_MILLIS < diaryEntry.getDepartureTime()) {
				diaryEntry.setDepartureTime(diaryEntry.getDepartureTime() - ONE_DAY_IN_MILLIS);
			}
			refreshTimeTextViews();
		}, hour, minute, true);

		timePicker.show();
	}

	private void saveEntry() {
		CrowdNotifier.updateCheckIn(diaryEntry.getId(), diaryEntry.getArrivalTime(), diaryEntry.getDepartureTime(),
				diaryEntry.getVenueInfo(), getContext());
		diaryStorage.updateEntry(diaryEntry);
	}

	private void hideInDiary() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.add(HideInDiaryDialogFragment.newInstance(diaryEntry.getId()), HideInDiaryDialogFragment.TAG)
				.commit();
	}

}
