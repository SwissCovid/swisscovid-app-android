package org.dpppt.android.app.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.R;
import org.dpppt.android.app.util.DateUtils;

public class ReportsPagerFragment extends Fragment {

	private static final String ARG_TYPE = "ARG_TYPE";
	private static final String ARG_TIMESTAMP = "ARG_TIMESTAMP";


	public enum Type {
		NO_REPORTS,
		POSSIBLE_INFECTION,
		NEW_CONTACT,
		POSITIVE_TESTED
	}


	private Type type;
	private long timestamp;

	public static ReportsPagerFragment newInstance(@NonNull Type type, long timestamp) {
		ReportsPagerFragment reportsPagerFragment = new ReportsPagerFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TYPE, type.ordinal());
		args.putLong(ARG_TIMESTAMP, timestamp);
		reportsPagerFragment.setArguments(args);
		return reportsPagerFragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		type = Type.values()[getArguments().getInt(ARG_TYPE)];
		timestamp = getArguments().getLong(ARG_TIMESTAMP);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View view = null;

		switch (type) {
			case NO_REPORTS:
				view = inflater.inflate(R.layout.fragment_reports_pager_no_reports, container, false);
				break;
			case POSSIBLE_INFECTION:
				view = inflater.inflate(R.layout.fragment_reports_pager_possible_infection, container, false);
				break;
			case NEW_CONTACT:
				view = inflater.inflate(R.layout.fragment_reports_pager_new_contact, container, false);
				break;
			case POSITIVE_TESTED:
				view = inflater.inflate(R.layout.fragment_reports_pager_positive_tested, container, false);
				break;
		}

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		if (timestamp != 0) {
			int daysDiff = DateUtils.getDaysDiff(timestamp);

			TextView date = view.findViewById(R.id.fragment_reports_pager_date);
			if (daysDiff == 0) {
				date.setText(getResources().getString(R.string.date_today));
			} else if (daysDiff == 1) {
				date.setText(getResources().getString(R.string.date_one_day_ago));
			} else {
				String dateStr = getResources().getString(R.string.date_days_ago);
				dateStr = dateStr.replace("{COUNT}", String.valueOf(daysDiff));
				date.setText(dateStr);
			}
		}
	}

}
