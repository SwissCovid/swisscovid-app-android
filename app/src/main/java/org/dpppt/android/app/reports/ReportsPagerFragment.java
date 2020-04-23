package org.dpppt.android.app.reports;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.dpppt.android.app.R;
import org.dpppt.android.app.viewmodel.TracingViewModel;

public class ReportsPagerFragment extends Fragment {

	public static ReportsPagerFragment newInstance() {
		return new ReportsPagerFragment();
	}

	public ReportsPagerFragment() { super(R.layout.fragment_reports); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

	}

}
