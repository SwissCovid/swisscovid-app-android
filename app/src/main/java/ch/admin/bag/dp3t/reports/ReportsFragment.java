/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.reports;

import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import java.util.List;
import java.util.TimeZone;

import org.dpppt.android.sdk.models.DayDate;
import org.dpppt.android.sdk.models.ExposureDay;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.contacts.ReactivateTracingReminderDialog;
import ch.admin.bag.dp3t.home.model.TracingStatusInterface;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.*;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;
import ch.admin.bag.dp3t.whattodo.WhereToTestDialogFragment;

public class ReportsFragment extends Fragment {

	public static ReportsFragment newInstance() {
		return new ReportsFragment();
	}

	private final int DAYS_TO_STAY_IN_QUARANTINE = 10;
	private final int MAX_EXPOSURE_AGE_TO_DO_A_TEST = 10;
	private final int MIN_EXPOSURE_AGE_TO_DO_A_TEST = 5;
	private final long ONE_DAY_IN_MILLIS = 24L * 60 * 60 * 1000;

	private TracingViewModel tracingViewModel;
	private SecureStorage secureStorage;

	private View headerFragmentContainer;
	private LockableScrollView scrollView;
	private View scrollViewFirstchild;

	private View healthyView;
	private View saveOthersView;
	private View leitfadenView;
	private View infectedView;

	private TextView xDaysLeftTextview;


	private boolean leitfadenJustOpened = false;

	public ReportsFragment() { super(R.layout.fragment_reports); }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		secureStorage = SecureStorage.getInstance(getContext());
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.reports_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		headerFragmentContainer = view.findViewById(R.id.header_fragment_container);
		scrollView = view.findViewById(R.id.reports_scrollview);
		scrollViewFirstchild = view.findViewById(R.id.reports_scrollview_firstChild);

		healthyView = view.findViewById(R.id.reports_healthy);
		saveOthersView = view.findViewById(R.id.reports_save_others);
		leitfadenView = view.findViewById(R.id.reports_leitfaden);
		infectedView = view.findViewById(R.id.reports_infected);

		xDaysLeftTextview = saveOthersView.findViewById(R.id.x_days_left_textview);

		Button openSwisscovidLeitfadenButton1 = leitfadenView.findViewById(R.id.card_encounters_button);
		View leitfadenInfoButton1 = leitfadenView.findViewById(R.id.leitfaden_info_button);
		Button openSwisscovidLeitfadenButton2 = saveOthersView.findViewById(R.id.card_encounters_button);
		View leitfadenInfoButton2 = saveOthersView.findViewById(R.id.leitfaden_info_button);

		openSwisscovidLeitfadenButton1.setOnClickListener(view1 -> openSwissCovidLeitfaden());
		leitfadenInfoButton1.setOnClickListener(v -> showLeitfadenInfo(openSwisscovidLeitfadenButton1.getText().toString()));
		openSwisscovidLeitfadenButton2.setOnClickListener(view1 -> openSwissCovidLeitfaden());
		leitfadenInfoButton2.setOnClickListener(v -> showLeitfadenInfo(openSwisscovidLeitfadenButton2.getText().toString()));

		setupFreeTestInfoBox(leitfadenView);
		setupFreeTestInfoBox(saveOthersView);

		View callHotlineButton1 = leitfadenView.findViewById(R.id.item_call_hotline_layout);
		View callHotlineButton2 = saveOthersView.findViewById(R.id.item_call_hotline_layout);
		callHotlineButton1.setOnClickListener(v -> callHotline());
		callHotlineButton2.setOnClickListener(v -> callHotline());

		Button faqButton1 = healthyView.findViewById(R.id.card_encounters_faq_button);
		Button faqButton2 = saveOthersView.findViewById(R.id.card_encounters_faq_button);
		Button faqButton3 = leitfadenView.findViewById(R.id.card_encounters_faq_button);
		Button faqButton4 = infectedView.findViewById(R.id.card_encounters_faq_button);

		faqButton1.setOnClickListener(v -> showFaq());
		faqButton2.setOnClickListener(v -> showFaq());
		faqButton3.setOnClickListener(v -> showFaq());
		faqButton4.setOnClickListener(v -> showFaq());

		View infoLinkHealthy = healthyView.findViewById(R.id.card_encounters_link);

		infoLinkHealthy.setOnClickListener(v -> openLink(R.string.no_meldungen_box_url));

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			healthyView.setVisibility(View.GONE);
			saveOthersView.setVisibility(View.GONE);
			leitfadenView.setVisibility(View.GONE);
			infectedView.setVisibility(View.GONE);

			ReportsHeaderFragment.Type headerType;
			int numExposureDays = 0;

			if (tracingStatusInterface.isReportedAsInfected()) {
				headerType = ReportsHeaderFragment.Type.POSITIVE_TESTED;
				infectedView.setVisibility(View.VISIBLE);

				// Show the onset date of the report
				long onsetDateInMillis = secureStorage.getPositiveReportOnsetDate();
				if (onsetDateInMillis > 0L) {
					infectedView.findViewById(R.id.card_encounters_faq_who_is_notified_container).setVisibility(View.VISIBLE);
					String formattedDate = DateUtils.getFormattedDateWrittenMonth(onsetDateInMillis, TimeZone.getTimeZone("UTC"));
					String faqText = getString(R.string.meldungen_positive_tested_faq2_text).replace("{ONSET_DATE}", formattedDate);
					Spannable formattedText = StringUtil.makePartiallyBold(faqText, formattedDate);
					((TextView) infectedView.findViewById(R.id.card_encounters_faq_who_is_notified)).setText(formattedText);
				} else {
					infectedView.findViewById(R.id.card_encounters_faq_who_is_notified_container).setVisibility(View.GONE);
				}

				infectedView.findViewById(R.id.delete_reports).setOnClickListener(v -> {
					AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle);
					builder.setMessage(R.string.delete_infection_dialog)
							.setPositiveButton(R.string.delete_infection_dialog_finish_button, (dialog, id) -> {
								tracingStatusInterface.resetInfectionStatus(getContext());
								secureStorage.setIsolationEndDialogTimestamp(-1L);
								secureStorage.setPositiveReportOnsetDate(-1L);
								getParentFragmentManager().popBackStack();
							})
							.setNegativeButton(R.string.cancel, (dialog, id) -> {
								//do nothing
							});
					builder.create();
					builder.show();
				});
				if (!tracingStatusInterface.canInfectedStatusBeReset(getContext())) {
					infectedView.findViewById(R.id.delete_reports).setVisibility(View.GONE);
				}
			} else if (tracingStatusInterface.wasContactReportedAsExposed()) {
				headerType = ReportsHeaderFragment.Type.POSSIBLE_INFECTION;
				numExposureDays = tracingStatusInterface.getExposureDays().size();
				boolean isOpenLeitfadenPending = secureStorage.isOpenLeitfadenPending();
				if (isOpenLeitfadenPending) {
					leitfadenView.setVisibility(View.VISIBLE);
				} else {
					saveOthersView.setVisibility(View.VISIBLE);
				}
				int daysLeft = DAYS_TO_STAY_IN_QUARANTINE - (int) tracingStatusInterface.getDaysSinceExposure();
				if (daysLeft > DAYS_TO_STAY_IN_QUARANTINE || daysLeft <= 0) {
					xDaysLeftTextview.setVisibility(View.GONE);
				} else if (daysLeft == 1) {
					xDaysLeftTextview.setText(R.string.date_in_one_day);
				} else {
					xDaysLeftTextview.setText(getString(R.string.date_in_days).replace("{COUNT}", String.valueOf(daysLeft)));
				}
				leitfadenView.findViewById(R.id.delete_reports)
						.setOnClickListener(v -> deleteNotifications(tracingStatusInterface));
				saveOthersView.findViewById(R.id.delete_reports)
						.setOnClickListener(v -> deleteNotifications(tracingStatusInterface));
			} else {
				healthyView.setVisibility(View.VISIBLE);
				headerType = ReportsHeaderFragment.Type.NO_REPORTS;
			}

			setupHeaderFragment(headerType, numExposureDays);
		});

		NotificationManager notificationManager =
				(NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NotificationUtil.NOTIFICATION_ID_CONTACT);
	}


	private void deleteNotifications(TracingStatusInterface tracingStatusInterface) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.NextStep_AlertDialogStyle);
		builder.setMessage(R.string.delete_notification_dialog)
				.setPositiveButton(R.string.delete_reports_button, (dialog, id) -> {
					tracingStatusInterface.resetExposureDays(getContext());
					getParentFragmentManager().popBackStack();
				})
				.setNegativeButton(R.string.cancel, (dialog, id) -> {
					//do nothing
				});
		builder.create();
		builder.show();
	}

	private void setupFreeTestInfoBox(@NonNull View view) {

		View testLocationsButton = view.findViewById(R.id.testlocations_link);
		testLocationsButton.setOnClickListener(v -> showWhereToTestDialog());

		TextView testCountdownTextView = view.findViewById(R.id.test_countdown_textview);

		List<ExposureDay> exposureDays = tracingViewModel.getAppStatusLiveData().getValue().getExposureDays();
		DayDate today = new DayDate();
		DayDate oldestExposure = null; // This includes only exposures that are newer than MIN_EXPOSURE_AGE_TO_DO_A_TEST

		for (ExposureDay exposureDay : exposureDays) {
			if (exposureDay.getExposedDate().addDays(MIN_EXPOSURE_AGE_TO_DO_A_TEST).isBeforeOrEquals(today) &&
					!exposureDay.getExposedDate().addDays(MAX_EXPOSURE_AGE_TO_DO_A_TEST).isBefore(today)) {
				testCountdownTextView.setText(R.string.meldungen_detail_free_test_now);
				return;
			}
			if (!exposureDay.getExposedDate().addDays(MIN_EXPOSURE_AGE_TO_DO_A_TEST).isBeforeOrEquals(today) &&
					exposureDay.getExposedDate().isBeforeOrEquals(today)) {
				if (oldestExposure == null || exposureDay.getExposedDate().isBefore(oldestExposure)) {
					oldestExposure = exposureDay.getExposedDate();
				}
			}
		}

		if (oldestExposure != null) {
			int daysSinceFirstExposure =
					(int) ((today.getStartOfDayTimestamp() - oldestExposure.getStartOfDayTimestamp()) / ONE_DAY_IN_MILLIS);
			int daysUntilTest = MIN_EXPOSURE_AGE_TO_DO_A_TEST - daysSinceFirstExposure;
			if (daysUntilTest == 1) {
				testCountdownTextView.setText(R.string.meldungen_detail_free_test_tomorrow);
			} else {
				testCountdownTextView.setText(getString(R.string.meldungen_detail_free_test_in_x_tagen)
						.replace("{COUNT}", String.valueOf(daysUntilTest)));
			}
		} else {
			testCountdownTextView.setVisibility(View.GONE);
		}
	}

	private void showWhereToTestDialog() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.add(WhereToTestDialogFragment.newInstance(), WhereToTestDialogFragment.class.getCanonicalName())
				.commit();
	}

	private void openLink(@StringRes int stringRes) {
		UrlUtil.openUrl(getContext(), getString(stringRes));
	}

	private void showFaq() {
		UrlUtil.openUrl(getContext(), getString(R.string.faq_button_url));
	}

	private void openSwissCovidLeitfaden() {
		leitfadenJustOpened = true;
		secureStorage.leitfadenOpened();
		List<ExposureDay> exposureDays = tracingViewModel.getAppStatusLiveData().getValue().getExposureDays();
		StringBuilder contactDates = new StringBuilder();
		String delimiter = "";
		for (ExposureDay exposureDay : exposureDays) {
			contactDates.append(delimiter);
			contactDates.append(exposureDay.getExposedDate().formatAsString());
			delimiter = ",";
		}
		UrlUtil.openUrl(getContext(), getString(R.string.swisscovid_leitfaden_url).replace("{CONTACT_DATES}", contactDates));
	}

	private void showLeitfadenInfo(String buttonTitleReplacementText) {
		String title = getString(R.string.leitfaden_infopopup_title);
		String subtitle = getString(R.string.leitfaden_infopopup_text).replace("{BUTTON_TITLE}", buttonTitleReplacementText);
		requireActivity().getSupportFragmentManager().beginTransaction().add(
				SimpleDismissableDialog.newInstance(title, subtitle),
				ReactivateTracingReminderDialog.class.getCanonicalName()).commit();
	}

	private void callHotline() {
		PhoneUtil.callHelpline(getContext());
	}

	@Override
	public void onResume() {
		super.onResume();

		if (leitfadenJustOpened) {
			leitfadenJustOpened = false;
			leitfadenView.setVisibility(View.GONE);
			saveOthersView.setVisibility(View.VISIBLE);
		}
	}


	public void doHeaderAnimation(View info, View image, Button button, View showAllButton, int numExposureDays) {
		secureStorage.setReportsHeaderAnimationPending(false);

		ViewGroup rootView = (ViewGroup) getView();

		scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
				rootView.getHeight(),
				scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
		scrollViewFirstchild.setVisibility(View.VISIBLE);

		rootView.post(() -> {

			AutoTransition autoTransition = new AutoTransition();
			autoTransition.setDuration(300);
			autoTransition.addListener(new Transition.TransitionListener() {
				@Override
				public void onTransitionStart(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionEnd(@NonNull Transition transition) {
					headerFragmentContainer.post(() -> setupScrollBehavior());
				}

				@Override
				public void onTransitionCancel(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionPause(@NonNull Transition transition) {

				}

				@Override
				public void onTransitionResume(@NonNull Transition transition) {

				}
			});

			TransitionManager.beginDelayedTransition(rootView, autoTransition);

			updateHeaderSize(false, numExposureDays);

			info.setVisibility(View.VISIBLE);
			image.setVisibility(View.GONE);
			button.setVisibility(View.GONE);
			if (numExposureDays <= 1) {
				showAllButton.setVisibility(View.GONE);
			} else {
				showAllButton.setVisibility(View.VISIBLE);
			}
		});
	}

	public void animateHeaderHeight(boolean showAll, int numExposureDays, View exposureDaysContainer, View dateTextView) {

		int exposureDayItemHeight = getResources().getDimensionPixelSize(R.dimen.header_reports_exposure_day_height);
		int endExposureDayTopPadding;
		int endHeaderHeight;
		int endDateTextHeight;
		int endExposureDaysContainerHeight;
		int endScrollViewPadding;
		if (showAll) {
			endExposureDayTopPadding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
			endHeaderHeight = Math.min(getScreenHeight() / 3 * 2,
					getResources().getDimensionPixelSize(R.dimen.header_height_reports_multiple_days) +
							exposureDayItemHeight * (numExposureDays - 1) + endExposureDayTopPadding);
			endDateTextHeight = 0;
			endExposureDaysContainerHeight =
					endHeaderHeight - getResources().getDimensionPixelSize(R.dimen.header_height_reports_multiple_days) +
							exposureDayItemHeight;
			endScrollViewPadding =
					endHeaderHeight - getResources().getDimensionPixelSize(R.dimen.top_item_header_overlap_reports_multiple_days);
		} else {
			endExposureDayTopPadding = 0;
			endHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height_reports_multiple_days);
			endDateTextHeight = exposureDayItemHeight;
			endExposureDaysContainerHeight = 0;
			endScrollViewPadding = getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports_multiple_days);
		}

		int startExposureDayTopPadding = exposureDaysContainer.getPaddingTop();
		int startHeaderHeight = headerFragmentContainer.getLayoutParams().height;
		int startScrollViewPadding = scrollViewFirstchild.getPaddingTop();
		int startDateTextHeight = dateTextView.getLayoutParams().height;
		int startExposureDaysContainerHeight = exposureDaysContainer.getLayoutParams().height;

		ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
		anim.addUpdateListener(v -> {
					float value = (float) v.getAnimatedValue();
					setHeight(headerFragmentContainer, value * (endHeaderHeight - startHeaderHeight) + startHeaderHeight);
					setHeight(dateTextView, value * (endDateTextHeight - startDateTextHeight) + startDateTextHeight);
					setHeight(exposureDaysContainer,
							value * (endExposureDaysContainerHeight - startExposureDaysContainerHeight) + startExposureDaysContainerHeight);
					scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
							(int) (value * (endScrollViewPadding - startScrollViewPadding) + startScrollViewPadding),
							scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
					exposureDaysContainer.setPadding(exposureDaysContainer.getPaddingLeft(),
							(int) (value * (endExposureDayTopPadding - startExposureDayTopPadding) + startExposureDayTopPadding),
							exposureDaysContainer.getPaddingRight(), exposureDaysContainer.getPaddingBottom());
					if (value == 0) {
						exposureDaysContainer.setVisibility(View.VISIBLE);
						dateTextView.setVisibility(View.VISIBLE);
					} else if (value == 1) {
						if (showAll) {
							dateTextView.setVisibility(View.GONE);
						} else {
							exposureDaysContainer.setVisibility(View.GONE);
						}
						headerFragmentContainer.post(this::setupScrollBehavior);
					}
				}
		);
		anim.setDuration(100);
		anim.start();
	}

	private int getScreenHeight() {
		return Resources.getSystem().getDisplayMetrics().heightPixels;
	}

	private void setHeight(View view, float height) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.height = (int) height;
		view.setLayoutParams(params);
	}

	private void updateHeaderSize(boolean isReportsHeaderAnimationPending, int numExposureDays) {
		ViewGroup.LayoutParams headerLp = headerFragmentContainer.getLayoutParams();
		if (isReportsHeaderAnimationPending) {
			headerLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
		} else if (numExposureDays <= 1) {
			headerLp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports);
			scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
					getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports),
					scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
		} else {
			headerLp.height = getResources().getDimensionPixelSize(R.dimen.header_height_reports_multiple_days);
			scrollViewFirstchild.setPadding(scrollViewFirstchild.getPaddingLeft(),
					getResources().getDimensionPixelSize(R.dimen.top_item_padding_reports_multiple_days),
					scrollViewFirstchild.getPaddingRight(), scrollViewFirstchild.getPaddingBottom());
		}
		headerFragmentContainer.setLayoutParams(headerLp);
		headerFragmentContainer.post(this::setupScrollBehavior);
	}

	private void setupScrollBehavior() {
		if (!isVisible()) return;

		Rect rect = new Rect();
		headerFragmentContainer.getDrawingRect(rect);
		scrollView.setScrollPreventRect(rect);

		int scrollRangePx = scrollViewFirstchild.getPaddingTop();
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);
		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = computeScrollAnimProgress(scrollY, scrollRangePx);
			headerFragmentContainer.setAlpha(1 - progress);
			headerFragmentContainer.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerFragmentContainer.setAlpha(1 - progress);
			headerFragmentContainer.setTranslationY(progress * translationRangePx);
		});
	}

	private float computeScrollAnimProgress(int scrollY, int scrollRange) {
		return Math.min(scrollY, scrollRange) / (float) scrollRange;
	}

	private void setupHeaderFragment(ReportsHeaderFragment.Type headerType, int numExposureDays) {

		boolean isReportsHeaderAnimationPending = secureStorage.isReportsHeaderAnimationPending();

		updateHeaderSize(isReportsHeaderAnimationPending, numExposureDays);

		if (isReportsHeaderAnimationPending) {
			scrollViewFirstchild.setVisibility(View.GONE);
		}

		headerFragmentContainer.post(this::setupScrollBehavior);

		Fragment header;
		switch (headerType) {
			case NO_REPORTS:
				header = ReportsHeaderFragment.newInstance(ReportsHeaderFragment.Type.NO_REPORTS, false);
				break;
			case POSSIBLE_INFECTION:
				header = ReportsHeaderFragment
						.newInstance(ReportsHeaderFragment.Type.POSSIBLE_INFECTION, isReportsHeaderAnimationPending);
				break;
			case POSITIVE_TESTED:
				header = ReportsHeaderFragment.newInstance(ReportsHeaderFragment.Type.POSITIVE_TESTED, false);
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + headerType);
		}

		getChildFragmentManager().beginTransaction()
				.replace(R.id.header_fragment_container, header)
				.commit();
	}

}
