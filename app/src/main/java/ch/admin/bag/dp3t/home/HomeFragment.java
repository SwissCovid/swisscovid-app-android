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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.TimeUnit;

import org.dpppt.android.sdk.TracingStatus;

import ch.admin.bag.dp3t.BuildConfig;
import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.CheckinOverviewFragment;
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel;
import ch.admin.bag.dp3t.checkin.checkinflow.CheckOutFragment;
import ch.admin.bag.dp3t.checkin.checkinflow.QrCodeScannerFragment;
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState;
import ch.admin.bag.dp3t.contacts.ContactsFragment;
import ch.admin.bag.dp3t.extensions.FragmentExtensionsKt;
import ch.admin.bag.dp3t.home.model.NotificationState;
import ch.admin.bag.dp3t.home.model.NotificationStateError;
import ch.admin.bag.dp3t.home.model.TracingState;
import ch.admin.bag.dp3t.home.model.TracingStatusInterface;
import ch.admin.bag.dp3t.home.views.HeaderView;
import ch.admin.bag.dp3t.inform.InformActivity;
import ch.admin.bag.dp3t.networking.models.InfoBoxModel;
import ch.admin.bag.dp3t.networking.models.InfoBoxModelCollection;
import ch.admin.bag.dp3t.reports.ReportsFragment;
import ch.admin.bag.dp3t.reports.ReportsOverviewFragment;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.*;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;
import ch.admin.bag.dp3t.whattodo.WtdInfolineAccessabilityDialogFragment;

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
	private View checkinCard;
	private View loadingView;
	private View covidCodeCard;

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
		checkinCard = view.findViewById(R.id.card_checkin);
		headerView = view.findViewById(R.id.home_header_view);
		scrollView = view.findViewById(R.id.home_scroll_view);
		loadingView = view.findViewById(R.id.loading_view);
		covidCodeCard = view.findViewById(R.id.card_covidcode);

		setupHeader();
		setupInfobox();
		setupTracingView();
		setupNotification();
		setupCheckinCard();
		setupNonProductionHint();
		setupScrollBehavior();
		setupCovidCodeCard();

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
		requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
		tracingCard.setForeground(requireContext().getDrawable(outValue.resourceId));

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			if (tracingStatusInterface.isReportedAsInfected() || secureStorage.getOnlyPartialOnboardingCompleted()) {
				tracingCard.findViewById(R.id.contacs_chevron).setVisibility(View.GONE);
				tracingCard.setOnClickListener(null);
				tracingCard.setForeground(null);
			} else {
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
		int checkinReports = crowdNotifierViewModel.getExposures().getValue().size();
		int tracingReports = tracingViewModel.getAppStatusLiveData().getValue().getExposureDays().size();
		boolean isReportedPositive = tracingViewModel.getTracingStatusInterface().isReportedAsInfected();
		if (((checkinReports > 0 && tracingReports > 0) || checkinReports > 1) && !isReportedPositive) {
			FragmentExtensionsKt
					.showFragment(this, ReportsOverviewFragment.newInstance(), R.id.main_fragment_container, false);
		} else {
			FragmentExtensionsKt.showFragment(this, ReportsFragment.newInstance(null), R.id.main_fragment_container, false);
		}
	}

	private void setupNotification() {
		cardNotifications.setOnClickListener(v -> showReportsFragment());

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface ->
				updateNotification(tracingStatusInterface, crowdNotifierViewModel.hasTraceKeyLoadingError().getValue()));
		crowdNotifierViewModel.hasTraceKeyLoadingError().observe(getViewLifecycleOwner(), hasTraceKeyLoadingError ->
				updateNotification(tracingViewModel.getAppStatusLiveData().getValue(), hasTraceKeyLoadingError));
	}

	private void updateNotification(TracingStatusInterface tracingStatusInterface, boolean hasCheckinKeyLoadingError) {
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
		} else if (tracingStatusInterface.wasContactReportedAsExposed() ||
				!crowdNotifierViewModel.getExposures().getValue().isEmpty()) {
			long daysSinceExposureTracing = tracingStatusInterface.getDaysSinceExposure();
			long daysSinceExposureCheckin = crowdNotifierViewModel.getDaysSinceExposure();
			long daysSinceExposure;
			if (daysSinceExposureTracing >= 0 && daysSinceExposureCheckin >= 0) {
				daysSinceExposure = Math.min(daysSinceExposureTracing, daysSinceExposureCheckin);
			} else {
				daysSinceExposure = Math.max(daysSinceExposureTracing, daysSinceExposureCheckin);
			}
			NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.EXPOSED, daysSinceExposure);
		} else {
			NotificationStateHelper.updateStatusView(reportStatusView, NotificationState.NO_REPORTS);
		}

		TracingStatus.ErrorState errorState = tracingStatusInterface.getReportErrorState();

		if (errorState != null && tracingStatusInterface.getTracingState().equals(TracingState.ACTIVE)) {
			TracingErrorStateHelper.updateErrorView(reportErrorView, errorState);
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
		} else if (hasCheckinKeyLoadingError) {
			TracingErrorStateHelper.updateErrorView(reportErrorView, CrowdNotifierErrorState.NETWORK);
			reportErrorView.findViewById(R.id.error_status_button).setOnClickListener(v -> {
				loadingView.setVisibility(VISIBLE);
				loadingView.animate()
						.alpha(1f)
						.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) { crowdNotifierViewModel.refreshTraceKeys(); }
						});
			});
		} else if (!isNotificationChannelEnabled(getContext(), NotificationUtil.NOTIFICATION_CHANNEL_ID)) {
			NotificationErrorStateHelper
					.updateNotificationErrorView(reportErrorView, NotificationStateError.NOTIFICATION_STATE_ERROR);
			reportErrorView.findViewById(R.id.error_status_button).setOnClickListener(v -> {
				openChannelSettings(NotificationUtil.NOTIFICATION_CHANNEL_ID);
			});
		} else {
			TracingErrorStateHelper.hideErrorView(reportErrorView);
		}
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

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			if (tracingStatusInterface.isReportedAsInfected()) {
				setupCheckinCardIsolationMode();
			} else {
				setupCheckinCardNonIsolationMode();
			}
		});
	}

	private void setupCheckinCardIsolationMode() {
		checkinCard.setOnClickListener(v -> showCheckinOverviewFragment());
		View checkinView = checkinCard.findViewById(R.id.checkin_view);
		View checkoutView = checkinCard.findViewById(R.id.checkout_view);
		View isolationView = checkinCard.findViewById(R.id.isolation_view);

		checkinView.setVisibility(View.GONE);
		checkoutView.setVisibility(View.GONE);
		isolationView.setVisibility(View.VISIBLE);

		TextView title = checkinCard.findViewById(R.id.status_title);
		title.setTextColor(ResourcesCompat.getColor(getResources(), R.color.purple_main, null));
		title.setText(R.string.checkin_ended_title);

		TextView subtitle = checkinCard.findViewById(R.id.status_text);
		subtitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.purple_main, null));
		subtitle.setText(R.string.checkin_ended_text);

		checkinCard.findViewById(R.id.status_background)
				.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.status_purple_bg)));

		((ImageView) checkinCard.findViewById(R.id.status_icon)).setImageResource(R.drawable.ic_stopp);

		((ImageView) checkinCard.findViewById(R.id.status_illustration)).setImageResource(R.drawable.ic_illu_checkin_ended);
	}

	private void setupCheckinCardNonIsolationMode() {
		checkinCard.setOnClickListener(v -> showCheckinOverviewFragment());

		View checkinView = checkinCard.findViewById(R.id.checkin_view);
		View checkoutView = checkinCard.findViewById(R.id.checkout_view);
		View isolationView = checkinCard.findViewById(R.id.isolation_view);
		TextView checkinVenueTitle = checkinCard.findViewById(R.id.checkin_venue_title);

		isolationView.setVisibility(View.GONE);
		crowdNotifierViewModel.isCheckedIn().observe(getViewLifecycleOwner(), isCheckedIn -> {
			if (isCheckedIn) {
				checkoutView.setVisibility(View.VISIBLE);
				checkinView.setVisibility(View.GONE);
				checkinVenueTitle.setText(crowdNotifierViewModel.getCheckInState().getVenueInfo().getTitle());
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

	private void setupCovidCodeCard() {
		Button covidCodeButton = covidCodeCard.findViewById(R.id.enter_covidcode_button);
		TextView covidCodeTitle = covidCodeCard.findViewById(R.id.enter_covidcode_title);
		TextView covidCodeText = covidCodeCard.findViewById(R.id.enter_covidcode_text);

		tracingViewModel.getAppStatusLiveData().observe(getViewLifecycleOwner(), tracingStatusInterface -> {
			if (tracingStatusInterface.isReportedAsInfected()) {
				covidCodeButton.setText(R.string.delete_infection_button);
				covidCodeTitle.setText(R.string.home_end_isolation_card_title);
				covidCodeText.setText(R.string.home_end_isolation_card_text);
				covidCodeButton.setOnClickListener(v -> {
					AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle);
					builder.setMessage(R.string.delete_infection_dialog)
							.setPositiveButton(R.string.delete_infection_dialog_finish_button, (dialog, id) -> {
								tracingStatusInterface.resetInfectionStatus(getContext());
								secureStorage.setIsolationEndDialogTimestamp(-1L);
								secureStorage.setPositiveReportOldestSharedKey(-1L);
								secureStorage.setPositiveReportOldestSharedKeyOrCheckin(-1L);
							})
							.setNegativeButton(R.string.cancel, (dialog, id) -> {
								//do nothing
							});
					builder.create();
					builder.show();
				});
			} else {
				covidCodeButton.setText(R.string.inform_code_title);
				covidCodeTitle.setText(R.string.home_covidcode_card_title);
				covidCodeText.setText(R.string.home_covidcode_card_text);
				covidCodeButton.setOnClickListener(v -> {
					if (crowdNotifierViewModel.isCheckedIn().getValue()) {
						showCannotEnterCovidcodeWhileCheckedInDialog();
					} else {
						Intent intent = new Intent(getActivity(), InformActivity.class);
						startActivity(intent);
					}
				});
			}
		});
	}

	private void showCannotEnterCovidcodeWhileCheckedInDialog() {
		new AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
				.setMessage(R.string.error_cannot_enter_covidcode_while_checked_in)
				.setPositiveButton(R.string.checkout_button_title, (dialog, id) -> showCheckOutFragment())
				.setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
				.create()
				.show();
	}

	private void showCheckOutFragment() {
		requireActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.modal_slide_enter, R.anim.modal_slide_exit, R.anim.modal_pop_enter,
						R.anim.modal_pop_exit)
				.replace(R.id.main_fragment_container, CheckOutFragment.newInstance())
				.addToBackStack(CheckOutFragment.class.getCanonicalName())
				.commit();
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
								secureStorage.setPositiveReportOldestSharedKeyOrCheckin(-1L);
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

}
