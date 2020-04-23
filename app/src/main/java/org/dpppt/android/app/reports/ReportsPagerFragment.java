package org.dpppt.android.app.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.R;

public class ReportsPagerFragment extends Fragment {

	public static final String ARG_TYPE = "ARG_TYPE";

	public enum Type {
		NO_REPORTS,
		POSSIBLE_INFECTION,
		NEW_CONTACT,
		POSITIVE_TESTED
	}

	private Type type;

	public static ReportsPagerFragment newNoReportsInstance(@NonNull Type type) {
		ReportsPagerFragment reportsPagerFragment = new ReportsPagerFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TYPE, type.ordinal());
		reportsPagerFragment.setArguments(args);
		return reportsPagerFragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		type = Type.values()[getArguments().getInt(ARG_TYPE)];
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		View view = null;

		switch (type){
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
				view = inflater.inflate(R.layout.fragment_reports_pager_new_contact, container, false);
				break;
		}

		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

	}

}
