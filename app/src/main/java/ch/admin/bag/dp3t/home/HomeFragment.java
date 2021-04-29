/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.logger.Logger;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.CheckinOverviewFragment;
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel;
import ch.admin.bag.dp3t.checkin.checkinflow.QrCodeScannerFragment;
import ch.admin.bag.dp3t.contacts.ContactsFragment;
import ch.admin.bag.dp3t.home.model.NotificationState;
import ch.admin.bag.dp3t.home.model.NotificationStateError;
import ch.admin.bag.dp3t.home.model.TracingState;
import ch.admin.bag.dp3t.home.model.TracingStatusInterface;
import ch.admin.bag.dp3t.home.views.HeaderView;
import ch.admin.bag.dp3t.networking.models.InfoBoxModel;
import ch.admin.bag.dp3t.networking.models.InfoBoxModelCollection;
import ch.admin.bag.dp3t.reports.ReportsFragment;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.travel.TravelFragment;
import ch.admin.bag.dp3t.travel.TravelUtils;
import ch.admin.bag.dp3t.util.*;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;
import ch.admin.bag.dp3t.whattodo.WtdInfolineAccessabilityDialogFragment;
import ch.admin.bag.dp3t.whattodo.WtdPositiveTestFragment;
import ch.admin.bag.dp3t.whattodo.WtdSymptomsFragment;

import static android.view.View.VISIBLE;

public class HomeFragment extends Fragment {

	private static final String TAG = "HomeFragment";
	private TracingViewModel tracingViewModel;
	private CrowdNotifierViewModel crowdNotifierViewModel;
	private HeaderView headerView;
	private ScrollView scrollView;

	private View infobox;
	private View tracingCard;
	private View cardNotifications;
	private View reportStatusBubble;
	private View reportStatusView;
	private View reportErrorView;
	private View travelCard;
	private View checkinCard;
	private View cardSymptomsFrame;
	private View cardTestFrame;
	private View cardSymptoms;
	private View cardTest;
	private View loadingView;

	private SecureStorage secureStorage;

	public HomeFragment() {
		super(R.layout.fragment_home);
	}

	public static HomeFragment newInstance() {
		return new HomeFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		secureStorage = SecureStorage.getInstance(getContext());

		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
		crowdNotifierViewModel = new ViewModelProvider(requireActivity()).get(CrowdNotifierViewModel.class);

		getChildFragmentManager()
				.beginTransaction()
				.add(R.id.status_container, TracingBoxFragment.newInstance(true))
				.commit();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		infobox = view.findViewById(R.id.card_infobox);
		tracingCard = view.findViewById(R.id.card_tracing);
		cardNotifications = view.findViewById(R.id.card_notifications);
		reportStatusBubble = view.findViewById(R.id.report_status_bubble);
		reportStatusView = reportStatusBubble.findViewById(R.id.report_status);
		reportErrorView = reportStatusBubble.findViewById(R.id.report_errors);
		travelCard = view.findViewById(R.id.card_travel);
		checkinCard = view.findViewById(R.id.card_checkin);
		headerView = view.findViewById(R.id.home_header_view);
		scrollView = view.findViewById(R.id.home_scroll_view);
		cardSymptoms = view.findViewById(R.id.card_what_to_do_symptoms);
		cardSymptomsFrame = view.findViewById(R.id.frame_card_symptoms);
		cardTest = view.findViewById(R.id.card_what_to_do_test);
		cardTestFrame = view.findViewById(R.id.frame_card_test);
		loadingView = view.findViewById(R.id.loading_view);

		setupHeader();
		setupInfobox();
		setupTracingView();
		setupNotification();
		setupCheckinCard();
		setupTravelCard();
		setupWhatToDo();
		setupNonProductionHint();
		setupScrollBehavior();

		showEndIsolationDialogIfNecessary();
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
		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), headerView::setState);
	}

	private void setupInfobox() {
		secureStorage.getInfoBoxLiveData().observe(getViewLifecycleOwner(), hasInfobox -> {
			hasInfobox = hasInfobox && secureStorage.getHasInfobox();
			InfoBoxModelCollection infoBoxModelCollection = secureStorage.getInfoBoxCollection();

			if (!hasInfobox || infoBoxModelCollection == null) {
				infobox.setVisibility(View.GONE);
				return;
			}

			InfoBoxModel infoBoxModel = infoBoxModelCollection.getInfoBox(getResources().getString(R.string.language_key));
			if (infoBoxModel == null) {
				infobox.setVisibility(View.GONE);
				return;
			}

			infobox.setVisibility(VISIBLE);

			String title = infoBoxModel.getTitle();
			TextView titleView = infobox.findViewById(R.id.infobox_title);
			if (title != null) {
				titleView.setText(title);
				titleView.setVisibility(VISIBLE);
			} else {
				titleView.setVisibility(View.GONE);
			}

			String text = infoBoxModel.getMsg();
			TextView textView = infobox.findViewById(R.id.infobox_text);
			if (text != null) {
				textView.setText(text);
				textView.setVisibility(VISIBLE);
			} else {
				textView.setVisibility(View.GONE);
			}

			String url = infoBoxModel.getUrl();
			String urlTitle = infoBoxModel.getUrlTitle();
			View linkGroup = infobox.findViewById(R.id.infobox_link_group);
			TextView linkView = infobox.findViewById(R.id.infobox_link_text);
			if (url != null) {
				linkView.setText(urlTitle != null ? urlTitle : url);
				linkGroup.setOnClickListener(v -> UrlUtil.openUrl(v.getContext(), url));
				linkGroup.setVisibility(VISIBLE);
				ImageView linkIcon = infobox.findViewById(R.id.infobox_link_icon);
				if (url.startsWith("tel://")) {
					linkIcon.setImageResource(R.drawable.ic_phone);
				} else {
					linkIcon.setImageResource(R.drawable.ic_launch);
				}
			} else {
				linkGroup.setVisibility(View.GONE);
			}

			String hearingImpairedInfo = infoBoxModel.getHearingImpairedInfo();
			View hearingImpairedView = infobox.findViewById(R.id.infobox_link_hearing_impaired);
			if (hearingImpairedInfo != null) {
				hearingImpairedView.setOnClickListener(v ->
						requireActivity().getSupportFragmentManager().beginTransaction()
								.add(WtdInfolineAccessabilityDialogFragment.newInstance(hearingImpairedInfo),
										WtdInfolineAccessabilityDialogFragment.class.getCanonicalName())
								.commit()
				);
				hearingImpairedView.setVisibility(VISIBLE);
			} else {
				hearingImpairedView.setVisibility(View.GONE);
			}

			boolean isDismissible = infoBoxModel.getDismissible();
			View dismissButton = infobox.findViewById(R.id.dismiss_button);
			if (isDismissible) {
				dismissButton.setVisibility(VISIBLE);
				dismissButton.setOnClickListener(v -> secureStorage.setHasInfobox(false));
			} else dismissButton.setVisibility(View.GONE);
		});
	}

	private void setupTracingView() {
		TypedValue outValue = new TypedValue();
		requireContext().getTheme().resolveAttribute(
				android.R.attr.selectableItemBackground, outValue, true);
		tracingCard.setForeground(requireContext().getDrawable(outValue.resourceId));

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			if (tracingStatusInterface.isReportedAsInfected()) {
				cardSymptomsFrame.setVisibility(View.GONE);
				cardTestFrame.setVisibility(View.GONE);
				tracingCard.findViewById(R.id.contacs_chevron).setVisibility(View.GONE);
				tracingCard.setOnClickListener(null);
				tracingCard.setForeground(null);
			} else {
				cardSymptomsFrame.setVisibility(VISIBLE);
				cardTestFrame.setVisibility(VISIBLE);
				tracingCard.findViewById(R.id.contacs_chevron).setVisibility(VISIBLE);
				tracingCard.setOnClickListener(v -> showContactsFragment());
			}
		});
	}

	private void showContactsFragment() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, ContactsFragment.newInstance())
				.addToBackStack(ContactsFragment.class.getCanonicalName())
				.commit();
	}

	private void showReportsFragment() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, ReportsFragment.newInstance())
				.addToBackStack(ReportsFragment.class.getCanonicalName())
				.commit();
	}

	private void setupNotification() {
		cardNotifications.setOnClickListener(v -> showReportsFragment());

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			//update status view
			if (loadingView.getVisibility() == VISIBLE) {
				loadingView.animate()
						.setStartDelay(getResources().getInteger(android.R.integer.config_mediumAnimTime))
						.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
						.alpha(0f)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								loadingView.setVisibility(View.GONE);
							}
						});
			} else {
				loadingView.setVisibility(View.GONE);
			}
			if (tracingStatusInterface.isReportedAsInfected()) {
				NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.POSITIVE_TESTED);
			} else if (tracingStatusInterface.wasContactReportedAsExposed()) {
				long daysSinceExposure = tracingStatusInterface.getDaysSinceExposure();
				NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.EXPOSED, daysSinceExposure);
			} else {
				NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.NO_REPORTS);
			}

			TracingStatus.ErrorState errorState = tracingStatusInterface.getReportErrorState();
			if (tracingStatusInterface.getTracingState().equals(TracingState.NOT_ACTIVE) &&
					!tracingStatusInterface.isReportedAsInfected()) {
				NotificationErrorStateHelper
						.updateNotificationErrorView(reportErrorView, NotificationStateError.TRACING_DEACTIVATED);
				reportErrorView.findViewById(R.id.error_status_button).setOnClickListener(v -> {
					enableTracing();
				});
			} else if (errorState != null) {
				TracingErrorStateHelper
						.updateErrorView(reportErrorView, errorState);
				reportErrorView.findViewById(R.id.error_status_button).setOnClickListener(v -> {
					loadingView.setVisibility(VISIBLE);
					loadingView.animate()
							.alpha(1f)
							.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
							.setListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) { tracingViewModel.sync(); }
							});
				});
			} else if (!isNotificationChannelEnabled(getContext(), NotificationUtil.NOTIFICATION_CHANNEL_ID)) {
				NotificationErrorStateHelper
						.updateNotificationErrorView(reportErrorView, NotificationStateError.NOTIFICATION_STATE_ERROR);
				reportErrorView.findViewById(R.id.error_status_button).setOnClickListener(v -> {
					openChannelSettings(NotificationUtil.NOTIFICATION_CHANNEL_ID);
				});
			} else {
				//hide errorview
				TracingErrorStateHelper.updateErrorView(reportErrorView, null);
			}
		});
	}

	private void openChannelSettings(String channelId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
			intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().getPackageName());
			startActivity(intent);
		} else {
			Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
			startActivity(intent);
		}
	}

	private boolean isNotificationChannelEnabled(Context context, @Nullable String channelId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			if (!TextUtils.isEmpty(channelId)) {
				NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationChannel channel = manager.getNotificationChannel(channelId);
				if (channel == null) {
					return manager.areNotificationsEnabled();
				}
				return channel.getImportance() != NotificationManager.IMPORTANCE_NONE &&
						!(!manager.areNotificationsEnabled() &&
								channel.getImportance() == NotificationManager.IMPORTANCE_DEFAULT) &&
						manager.areNotificationsEnabled();
			}
			return true;
		} else {
			return NotificationManagerCompat.from(context).areNotificationsEnabled();
		}
	}

	private void setupCheckinCard() {

		checkinCard.setOnClickListener(v -> showCheckinOverviewFragment());

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

	private void showCheckedInFragment() {
		//TODO
	}

	private void showQrCodeScannerFragment() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, QrCodeScannerFragment.newInstance())
				.addToBackStack(QrCodeScannerFragment.class.getCanonicalName())
				.commit();
	}

	private void showCheckinOverviewFragment() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
				.replace(R.id.main_fragment_container, CheckinOverviewFragment.newInstance())
				.addToBackStack(CheckinOverviewFragment.class.getCanonicalName())
				.commit();
	}

	private void setupTravelCard() {
		travelCard.setOnClickListener(
				v -> requireActivity().getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.main_fragment_container, TravelFragment.newInstance())
						.addToBackStack(TravelFragment.class.getCanonicalName())
						.commit()
		);

		List<String> countries = secureStorage.getInteropCountries();
		if (!countries.isEmpty()) {
			travelCard.setVisibility(VISIBLE);
			Flow flowConstraint = travelCard.findViewById(R.id.travel_flags_flow);
			TravelUtils.inflateFlagFlow(flowConstraint, countries);
		} else {
			travelCard.setVisibility(View.GONE);
		}
	}

	private void setupWhatToDo() {
		cardSymptoms.setOnClickListener(
				v -> requireActivity().getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.main_fragment_container, WtdSymptomsFragment.newInstance())
						.addToBackStack(WtdSymptomsFragment.class.getCanonicalName())
						.commit());
		cardTest.setOnClickListener(
				v -> requireActivity().getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.main_fragment_container, WtdPositiveTestFragment.newInstance())
						.addToBackStack(WtdPositiveTestFragment.class.getCanonicalName())
						.commit());
	}

	private void setupNonProductionHint() {
		View nonProduction = requireView().findViewById(R.id.non_production_message);
		if (BuildConfig.IS_FLAVOR_PROD || BuildConfig.IS_FLAVOR_ABNAHME) {
			nonProduction.setVisibility(View.GONE);
		} else {
			nonProduction.setVisibility(VISIBLE);
		}
	}

	private void setupScrollBehavior() {
		int scrollRangePx = getResources().getDimensionPixelSize(R.dimen.top_item_padding);
		int translationRangePx = -getResources().getDimensionPixelSize(R.dimen.spacing_huge);

		scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			float progress = UiUtils.computeScrollAnimProgress(scrollY, scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
		scrollView.post(() -> {
			float progress = UiUtils.computeScrollAnimProgress(scrollView.getScrollY(), scrollRangePx);
			headerView.setAlpha(1 - progress);
			headerView.setTranslationY(progress * translationRangePx);
		});
	}

	private void showEndIsolationDialogIfNecessary() {
		Observer<TracingStatusInterface> observer = new Observer<TracingStatusInterface>() {
			@Override
			public void onChanged(TracingStatusInterface tracingStatusInterface) {
				long isolationEndDialogTimestamp = secureStorage.getIsolationEndDialogTimestamp();
				if (isolationEndDialogTimestamp != -1L && System.currentTimeMillis() > isolationEndDialogTimestamp &&
						tracingStatusInterface.isReportedAsInfected()) {
					new AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
							.setTitle(R.string.homescreen_isolation_ended_popup_title)
							.setMessage(R.string.homescreen_isolation_ended_popup_text)
							.setPositiveButton(R.string.answer_yes, (dialog, which) -> {
								tracingStatusInterface.resetInfectionStatus(getContext());
								secureStorage.setIsolationEndDialogTimestamp(-1L);
								secureStorage.setPositiveReportOldestSharedKey(-1L);
							})
							.setNegativeButton(R.string.answer_no, (dialog, which) -> {
								long newTimestamp = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
								secureStorage.setIsolationEndDialogTimestamp(newTimestamp);
							})
							.setCancelable(false)
							.show();
				}
				tracingViewModel.getAppStatusLiveData().removeObserver(this);
			}
		};

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), observer);
	}

	private void enableTracing() {
		Activity activity = getActivity();
		if (activity == null) {
			return;
		}

		tracingViewModel.enableTracing(activity,
				() -> { },
				e -> {
					String message = ENExceptionHelper.getErrorMessage(e, activity);
					Logger.e(TAG, message);
					new AlertDialog.Builder(activity, R.style.NextStep_AlertDialogStyle)
							.setTitle(R.string.android_en_start_failure)
							.setMessage(message)
							.setPositiveButton(R.string.android_button_ok, (dialog, which) -> {})
							.show();
				},
				() -> {
					// cancelled
				});
	}

}
