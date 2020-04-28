/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Collection;

import org.dpppt.android.app.BuildConfig;
import org.dpppt.android.app.R;
import org.dpppt.android.app.contacts.ContactsFragment;
import org.dpppt.android.app.debug.DebugFragment;
import org.dpppt.android.app.main.model.NotificationState;
import org.dpppt.android.app.main.views.HeaderView;
import org.dpppt.android.app.reports.ReportsFragment;
import org.dpppt.android.app.util.NotificationStateHelper;
import org.dpppt.android.app.util.TracingErrorStateHelper;
import org.dpppt.android.app.viewmodel.TracingViewModel;
import org.dpppt.android.app.whattodo.WtdPositiveTestFragment;
import org.dpppt.android.app.whattodo.WtdSymptomsFragment;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.util.FileUploadRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.VISIBLE;

public class HomeFragment extends Fragment {

	private TracingViewModel tracingViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private View tracingCard;
	private View cardNotifications;
	private View reportStatusBubble;
	private View reportStatusView;
	private View reportErrorView;
	private View cardSymptomsFrame;
	private View cardTestFrame;
	private View cardSymptoms;
	private View cardTest;

	public HomeFragment() {
		super(R.layout.fragment_home);
	}

	public static HomeFragment newInstance() {
		Bundle args = new Bundle();
		HomeFragment fragment = new HomeFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		getChildFragmentManager()
				.beginTransaction()
				.add(R.id.status_container, TracingBoxFragment.newInstance())
				.commit();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		tracingCard = view.findViewById(R.id.card_tracing);
		cardNotifications = view.findViewById(R.id.card_notifications);
		reportStatusBubble = view.findViewById(R.id.report_status_bubble);
		reportStatusView = reportStatusBubble.findViewById(R.id.report_status);
		reportErrorView = reportStatusBubble.findViewById(R.id.report_errors);
		headerView = view.findViewById(R.id.home_header_view);
		scrollView = view.findViewById(R.id.home_scroll_view);
		cardSymptoms = view.findViewById(R.id.card_what_to_do_symptoms);
		cardSymptomsFrame = view.findViewById(R.id.frame_card_symptoms);
		cardTest = view.findViewById(R.id.card_what_to_do_test);
		cardTestFrame = view.findViewById(R.id.frame_card_test);

		setupHeader();
		setupTracingView();
		setupNotification();
		setupWhatToDo();
		setupDebugButton();
		setupScrollBehavior();
	}

	@Override
	public void onStart() {
		super.onStart();
		tracingViewModel.invalidateTracingStatus();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		headerView.stopAnimation();
	}

	private void setupHeader() {
		tracingViewModel.getAppStatusLiveData()
				.observe(getViewLifecycleOwner(), headerView::setState);
	}

	private void setupTracingView() {

		tracingCard.setOnClickListener(v -> showContactsFragment());

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			if (tracingStatusInterface.isReportedAsInfected()) {
				cardSymptomsFrame.setVisibility(View.GONE);
				cardTestFrame.setVisibility(View.GONE);
				tracingCard.findViewById(R.id.contacs_chevron).setVisibility(View.GONE);
				tracingCard.setOnClickListener(null);
			} else {
				cardSymptomsFrame.setVisibility(VISIBLE);
				cardTestFrame.setVisibility(VISIBLE);
				tracingCard.findViewById(R.id.contacs_chevron).setVisibility(VISIBLE);
				tracingCard.setOnClickListener(v -> showContactsFragment());
			}
		});
	}

	private void showContactsFragment() {
		getParentFragmentManager().beginTransaction()
				.replace(R.id.main_fragment_container, ContactsFragment.newInstance())
				.addToBackStack(ContactsFragment.class.getCanonicalName())
				.commit();
	}


	private void setupNotification() {
		cardNotifications.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
						.addToBackStack(ReportsFragment.class.getCanonicalName())
						.commit());

		tracingViewModel.getSelfOrContactExposedLiveData().observe(getViewLifecycleOwner(),
				selfOrContactExposed -> {
					if (selfOrContactExposed.first) {
						NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.POSITIVE_TESTED);
					} else if (selfOrContactExposed.second) {
						NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.EXPOSED);
					} else {
						NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.NO_REPORTS);
					}
					if (tracingViewModel.getTracingStatusLiveData().getValue() != null) {
						Collection<TracingStatus.ErrorState> errorStates =
								tracingViewModel.getTracingStatusLiveData().getValue().getErrors();
						if (errorStates != null && errorStates.size() > 0) {
							TracingStatus.ErrorState errorState = TracingErrorStateHelper.getErrorStateForReports(errorStates);
							TracingErrorStateHelper.updateErrorView(reportErrorView, errorState);
						} else {
							TracingErrorStateHelper.updateErrorView(reportErrorView, null);
						}
					} else {
						TracingErrorStateHelper.updateErrorView(reportErrorView, null);
					}
				});
	}

	private void setupWhatToDo() {

		cardSymptoms.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, WtdSymptomsFragment.newInstance())
						.addToBackStack(WtdSymptomsFragment.class.getCanonicalName())
						.commit());
		cardTest.setOnClickListener(
				v -> getParentFragmentManager().beginTransaction()
						.replace(R.id.main_fragment_container, WtdPositiveTestFragment.newInstance())
						.addToBackStack(WtdPositiveTestFragment.class.getCanonicalName())
						.commit());
	}

	private void setupDebugButton() {
		View debugButton = getView().findViewById(R.id.main_button_debug);
		if (BuildConfig.IS_DEV) {
			debugButton.setVisibility(VISIBLE);
			debugButton.setOnClickListener(v -> DebugFragment.startDebugFragment(getParentFragmentManager()));
		} else {
			debugButton.setVisibility(View.GONE);
		}

		View debugUploadButton = getView().findViewById(R.id.main_button_upload_debug_data);
		debugUploadButton.setOnClickListener(view -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle("Identifier");
			final EditText input = new EditText(getContext());
			builder.setView(input);
			builder.setPositiveButton("OK", (dialog, which) -> {
				String name = input.getText().toString();
				ProgressDialog progressDialog = ProgressDialog.show(getContext(), "Upload", "");
				new FileUploadRepository()
						.uploadDatabase(getContext(), name,
								new Callback<Void>() {
									@Override
									public void onResponse(Call<Void> call, Response<Void> response) {
										progressDialog.hide();
										Toast.makeText(getContext(), "Upload success!", Toast.LENGTH_LONG).show();
									}

									@Override
									public void onFailure(Call<Void> call, Throwable t) {
										t.printStackTrace();
										progressDialog.hide();
										Toast.makeText(getContext(), "Upload failed!", Toast.LENGTH_LONG).show();
									}
								});
			});
			builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
			builder.show();
		});
	}

	private void setupScrollBehavior() {

		int scrollRangePx = getResources().getDimensionPixelSize(R.dimen.top_item_padding);
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);
		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = computeScrollAnimProgress(scrollY, scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

}
