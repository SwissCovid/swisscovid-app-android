package ch.admin.bag.dp3t.contacts;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.ReminderHelper;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class ReactivateTracingReminderDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener {

	public static ReactivateTracingReminderDialog newInstance() { return new ReactivateTracingReminderDialog(); }

	private static final long HOUR_IN_MILLISECONDS = 1000L * 60 * 60;
	private static final long MINUTE_IN_MILLISECONDS = 1000L * 60;
	private TracingViewModel tracingViewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onResume() {
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		super.onResume();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_reactivate_tracing_reminder, container);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		RadioGroup reminderRadioGroup = view.findViewById(R.id.reactivate_tracing_reminder_radio_group);
		RadioButton fourHoursButton = view.findViewById(R.id.reactivate_tracing_reminder_radio_option_four_hours);
		RadioButton eightHoursButton = view.findViewById(R.id.reactivate_tracing_reminder_radio_option_eight_hours);
		RadioButton twelveHoursButton = view.findViewById(R.id.reactivate_tracing_reminder_radio_option_twelve_hours);
		RadioButton noReminderButton = view.findViewById(R.id.reactivate_tracing_reminder_radio_option_no_reminder);

		fourHoursButton.setOnCheckedChangeListener(this);
		eightHoursButton.setOnCheckedChangeListener(this);
		twelveHoursButton.setOnCheckedChangeListener(this);
		noReminderButton.setOnCheckedChangeListener(this);
		reminderRadioGroup.check(R.id.reactivate_tracing_reminder_radio_option_four_hours);

		view.findViewById(R.id.reactivate_tracing_reminder_cancel_button).setOnClickListener(v -> dismiss());
		view.findViewById(R.id.reactivate_tracing_reminder_ok_button).setOnClickListener(v -> {
			tracingViewModel.disableTracing();
			switch (reminderRadioGroup.getCheckedRadioButtonId()) {
				case R.id.reactivate_tracing_reminder_radio_option_four_hours:
					ReminderHelper
							.setTracingActivationReminder(System.currentTimeMillis() +
											4 * (BuildConfig.FLAVOR.equals("dev") ? MINUTE_IN_MILLISECONDS : HOUR_IN_MILLISECONDS),
									requireContext());
					break;
				case R.id.reactivate_tracing_reminder_radio_option_eight_hours:
					ReminderHelper
							.setTracingActivationReminder(System.currentTimeMillis() +
											8 * (BuildConfig.FLAVOR.equals("dev") ? MINUTE_IN_MILLISECONDS : HOUR_IN_MILLISECONDS),
									requireContext());
					break;
				case R.id.reactivate_tracing_reminder_radio_option_twelve_hours:
					ReminderHelper
							.setTracingActivationReminder(System.currentTimeMillis() +
											12 * (BuildConfig.FLAVOR.equals("dev") ? MINUTE_IN_MILLISECONDS :
												  HOUR_IN_MILLISECONDS),
									requireContext());
					break;
				default:
					break;
			}
			dismiss();
		});
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		tracingViewModel.invalidateTracingStatus();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		buttonView.setTypeface(isChecked ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
	}

}
