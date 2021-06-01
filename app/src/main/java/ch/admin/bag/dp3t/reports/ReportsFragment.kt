/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.reports

import android.animation.ValueAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage
import ch.admin.bag.dp3t.contacts.ReactivateTracingReminderDialog
import ch.admin.bag.dp3t.databinding.FragmentReportsBinding
import ch.admin.bag.dp3t.databinding.FragmentReportsHeaderPossibleInfectionBinding
import ch.admin.bag.dp3t.extensions.getDetailsString
import ch.admin.bag.dp3t.home.model.TracingStatusInterface
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.*
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import java.util.*
import kotlin.math.min

private const val ARG_CHECK_IN_ID = "ARG_CHECK_IN_ID"
private const val ARG_SHOW_TRACING_REPORT_DETAILS = "ARG_SHOW_TRACING_REPORT_DETAILS"

class ReportsFragment : Fragment() {

	companion object {

		@JvmStatic
		fun newInstance(reportItem: ReportItem? = null): ReportsFragment {
			return ReportsFragment().apply {
				when (reportItem) {
					is ProximityTracingReportItem -> arguments = bundleOf(ARG_SHOW_TRACING_REPORT_DETAILS to true)
					is CheckinReportItem -> arguments = bundleOf(ARG_CHECK_IN_ID to reportItem.exposure.id)
				}
			}
		}
	}

	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()
	private val tracingViewModel: TracingViewModel by activityViewModels()
	private val secureStorage by lazy { SecureStorage.getInstance(requireContext()) }
	private val diaryStorage by lazy { DiaryStorage.getInstance(requireContext()) }


	private lateinit var binding: FragmentReportsBinding

	private var leitfadenJustOpened = false

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentReportsBinding.inflate(inflater)
		return binding.apply {
			reportsToolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
			val showTracingReports = arguments?.getBoolean(ARG_SHOW_TRACING_REPORT_DETAILS, false) == true
			val showCheckinReport = arguments?.getLong(ARG_CHECK_IN_ID) != null

			crowdNotifierViewModel.exposures.observe(viewLifecycleOwner) { checkinExposures ->
				val tracingStatusInterface = tracingViewModel.appStatusLiveData.value!!
				setupState(
					when {
						showTracingReports -> State.POSSIBLE_INFECTION_TRACING_REPORTS
						showCheckinReport -> State.POSSIBLE_INFECTION_CHECKIN_REPORT
						tracingStatusInterface.isReportedAsInfected -> State.POSITIVE_TESTED
						tracingStatusInterface.wasContactReportedAsExposed() && checkinExposures.isEmpty() -> State.POSSIBLE_INFECTION_TRACING_REPORTS
						!tracingStatusInterface.wasContactReportedAsExposed() && checkinExposures.isNotEmpty() -> State.POSSIBLE_INFECTION_CHECKIN_REPORT
						else -> State.NO_REPORTS
					}, tracingStatusInterface
				)

			}
		}.root
	}

	private fun setupHeader(state: State) {
		binding.apply {
			headerFragmentContainer.removeAllViews()
			val isReportsHeaderAnimationPending =
				secureStorage.isReportsHeaderAnimationPending && (state == State.POSSIBLE_INFECTION_TRACING_REPORTS || state == State.POSSIBLE_INFECTION_CHECKIN_REPORT)
			val exposureDays = tracingViewModel.appStatusLiveData.value?.exposureDays ?: listOf()
			updateHeaderSize(isReportsHeaderAnimationPending, state, exposureDays.size)
			if (isReportsHeaderAnimationPending) {
				reportsScrollviewFirstChild.visibility = View.GONE
			}
			headerFragmentContainer.post { setupScrollBehavior() }

			val headerView =
				LayoutInflater.from(requireContext()).inflate(state.headerLayoutResource, headerFragmentContainer, true)
			if (state == State.NO_REPORTS || state == State.POSITIVE_TESTED) return

			FragmentReportsHeaderPossibleInfectionBinding.bind(headerView).apply {

				if (state == State.POSSIBLE_INFECTION_CHECKIN_REPORT) {
					headerShowAllButton.isVisible = false
					headerDate.isVisible = false
					headerSlogan.isVisible = true
					datesContainer.isVisible = false
					headerSubtitle.isVisible = false
				} else {
					if (exposureDays.isNotEmpty()) {
						headerDate.text =
							StringUtil.getReportDateString(
								exposureDays[exposureDays.size - 1].exposedDate.getStartOfDay(TimeZone.getDefault()),
								withDiff = true,
								withPrefix = true,
								requireContext()
							)
					}
					datesContainer.removeAllViews()
					for (exposureDay in exposureDays) {
						val itemView = LayoutInflater.from(context)
							.inflate(R.layout.item_reports_exposure_day, datesContainer, false)
						val itemDate = itemView.findViewById<TextView>(R.id.exposure_day_textview)
						itemDate.text = StringUtil.getReportDateString(
							exposureDay.exposedDate.getStartOfDay(TimeZone.getDefault()),
							withDiff = false,
							withPrefix = true,
							requireContext()
						)
						datesContainer.addView(itemView, 0)
					}

					headerShowAllButton.isVisible = exposureDays.size > 1

					headerShowAllButton.setOnClickListener {
						if (datesScrollView.visibility == View.VISIBLE) {
							headerSubtitle.setText(R.string.meldung_detail_exposed_subtitle_last_encounter)
							headerShowAllButton.setText(R.string.meldung_detail_exposed_show_all_button)
							animateHeaderHeight(false, exposureDays.size, datesScrollView, headerDate)
						} else {
							headerSubtitle.setText(R.string.meldung_detail_exposed_subtitle_all_encounters)
							headerShowAllButton.setText(R.string.meldung_detail_exposed_show_less_button)
							animateHeaderHeight(true, exposureDays.size, datesScrollView, headerDate)
						}
					}
					headerShowAllButton.paintFlags = headerShowAllButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
				}


				if (isReportsHeaderAnimationPending) {
					headerInfo.isVisible = false
					headerShowAllButton.isVisible = false
					headerImage.isVisible = true
					headerSubtitle.isVisible = false
					headerSlogan.isVisible = true
					headerContinueButton.isVisible = true
					headerDate.isVisible = false
					headerTitle.setText(R.string.meldung_detail_exposed_title)
					headerContinueButton.setOnClickListener { doHeaderCollapseAnimation(this, exposureDays.size, state) }
				}
			}
		}
	}

	private fun setupState(state: State, tracingStatusInterface: TracingStatusInterface) {

		binding.apply {
			reportsHealthy.root.isVisible = state == State.NO_REPORTS
			reportsInfected.root.isVisible = state == State.POSITIVE_TESTED
			reportsLeitfaden.root.isVisible = state == State.POSSIBLE_INFECTION_TRACING_REPORTS
			reportsCheckinReport.root.isVisible = state == State.POSSIBLE_INFECTION_CHECKIN_REPORT
		}
		when (state) {
			State.NO_REPORTS -> {
				binding.reportsHealthy.apply {
					cardEncountersLink.setOnClickListener { openLink(R.string.no_meldungen_box_url) }
					faqButton.setOnClickListener { showFaq() }
				}
			}
			State.POSSIBLE_INFECTION_TRACING_REPORTS -> {
				binding.reportsLeitfaden.apply {
					reportFurtherInformation.itemCallHotlineLayout.setOnClickListener { callHotline() }
					reportFurtherInformation.testsExternalLink.setOnClickListener { showTestInformation() }
					faqButton.setOnClickListener { showFaq() }
					val isOpenLeitfadenPending = secureStorage.isOpenLeitfadenPending
					fillLeitfadenNowButton.isVisible = isOpenLeitfadenPending
					zumLeitfadenButton.isVisible = !isOpenLeitfadenPending
					if (isOpenLeitfadenPending) {
						fillLeitfadenNowButton.setOnClickListener { openSwissCovidLeitfaden() }
						leitfadenInfoButton.setOnClickListener { showLeitfadenInfo(fillLeitfadenNowButton.text.toString()) }
					} else {
						zumLeitfadenButton.setOnClickListener { openSwissCovidLeitfaden() }
						leitfadenInfoButton.setOnClickListener { showLeitfadenInfo(zumLeitfadenButton.text.toString()) }
					}
					deleteReports.setOnClickListener { deleteNotifications(tracingStatusInterface) }
				}
			}
			State.POSITIVE_TESTED -> {
				val oldestSharedKeyDateMillis = secureStorage.positiveReportOldestSharedKeyOrCheckin
				binding.reportsInfected.apply {
					faqButton.setOnClickListener { showFaq() }
					cardEncountersFaqWhoIsNotifiedContainer.isVisible = oldestSharedKeyDateMillis > 0L
					val formattedDate =
						DateUtils.getFormattedDateWrittenMonth(oldestSharedKeyDateMillis, TimeZone.getTimeZone("UTC"))
					val faqText = getString(R.string.meldungen_positive_tested_faq2_text).replace("{ONSET_DATE}", formattedDate)
					val formattedText = StringUtil.makePartiallyBold(faqText, formattedDate)
					cardEncountersFaqWhoIsNotified.text = formattedText
					deleteReports.setOnClickListener { showDeleteReportConfirmationDialog() }
					deleteReports.isVisible = tracingStatusInterface.canInfectedStatusBeReset(requireContext())
				}
			}
			State.POSSIBLE_INFECTION_CHECKIN_REPORT -> {
				binding.reportsCheckinReport.apply {
					(crowdNotifierViewModel.getExposureWithId(arguments?.getLong(ARG_CHECK_IN_ID) ?: -1)
						?: crowdNotifierViewModel.latestExposure)?.let { exposureEvent ->
						deleteReport.setOnClickListener { deleteNotifications(tracingStatusInterface, exposureEvent.id) }
						reportDetails.text = exposureEvent.getDetailsString(requireContext())

						val diaryEntry: DiaryEntry? = diaryStorage.getDiaryEntryWithId(exposureEvent.id)
						place.isVisible = diaryEntry != null
						place.text = diaryEntry?.venueInfo?.title

					}
					faqButton.setOnClickListener { showFaq() }
					reportFurtherInformation.testsExternalLink.setOnClickListener { showTestInformation() }
					reportFurtherInformation.phoneSection.isVisible = false
				}
			}
		}
		val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(NotificationUtil.NOTIFICATION_ID_CONTACT)
		setupHeader(state)
	}

	private fun showDeleteReportConfirmationDialog() {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.delete_infection_dialog)
			.setPositiveButton(R.string.delete_infection_dialog_finish_button) { _, _ ->
				tracingViewModel.appStatusLiveData.value?.resetInfectionStatus(context)
				secureStorage.isolationEndDialogTimestamp = -1L
				secureStorage.positiveReportOldestSharedKey = -1L
				secureStorage.positiveReportOldestSharedKeyOrCheckin = -1L
				parentFragmentManager.popBackStack()
			}
			.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
			.create()
			.show()
	}

	private fun deleteNotifications(tracingStatusInterface: TracingStatusInterface, checkinId: Long? = null) {
		AlertDialog.Builder(requireActivity(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.delete_notification_dialog)
			.setPositiveButton(R.string.delete_reports_button) { _, _ ->
				if (checkinId == null) {
					tracingStatusInterface.resetExposureDays(context)
				} else {
					crowdNotifierViewModel.removeExposure(checkinId)
				}
				parentFragmentManager.popBackStack()
			}
			.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
			.create()
			.show()
	}

	private fun openLink(@StringRes stringRes: Int) {
		UrlUtil.openUrl(context, getString(stringRes))
	}

	private fun showFaq() {
		UrlUtil.openUrl(context, getString(R.string.faq_button_url))
	}

	private fun openSwissCovidLeitfaden() {
		leitfadenJustOpened = true
		secureStorage.leitfadenOpened()
		val contactDates = StringBuilder()
		var delimiter = ""
		tracingViewModel.appStatusLiveData.value?.exposureDays?.forEach {
			contactDates.append(delimiter)
			contactDates.append(it.exposedDate.formatAsString())
			delimiter = ","
		}
		UrlUtil.openUrl(context, getString(R.string.swisscovid_leitfaden_url).replace("{CONTACT_DATES}", contactDates.toString()))
	}

	private fun showLeitfadenInfo(buttonTitleReplacementText: String) {
		val title = getString(R.string.leitfaden_infopopup_title)
		val subtitle = getString(R.string.leitfaden_infopopup_text).replace("{BUTTON_TITLE}", buttonTitleReplacementText)
		requireActivity().supportFragmentManager.beginTransaction().add(
			SimpleDismissableDialog.newInstance(title, subtitle),
			ReactivateTracingReminderDialog::class.java.canonicalName
		).commit()
	}

	private fun showTestInformation() {
		UrlUtil.openUrl(context, secureStorage.getTestInformationUrl(getString(R.string.language_key)))
	}

	private fun callHotline() {
		PhoneUtil.callHelpline(context)
	}

	override fun onResume() {
		super.onResume()
		if (leitfadenJustOpened) {
			leitfadenJustOpened = false
			setupState(State.POSSIBLE_INFECTION_TRACING_REPORTS, tracingViewModel.tracingStatusInterface)
		}
	}

	private fun doHeaderCollapseAnimation(
		headerBinding: FragmentReportsHeaderPossibleInfectionBinding,
		numExposureDays: Int,
		state: State
	) {
		secureStorage.isReportsHeaderAnimationPending = false
		binding.apply {
			reportsScrollviewFirstChild.updatePadding(top = root.height)
			reportsScrollviewFirstChild.isVisible = true
			root.post {
				val autoTransition = AutoTransition()
				autoTransition.duration = 300
				autoTransition.addListener(object : Transition.TransitionListener {
					override fun onTransitionStart(transition: Transition) {}
					override fun onTransitionEnd(transition: Transition) {
						headerFragmentContainer.post { setupScrollBehavior() }
					}

					override fun onTransitionCancel(transition: Transition) {}
					override fun onTransitionPause(transition: Transition) {}
					override fun onTransitionResume(transition: Transition) {}
				})
				TransitionManager.beginDelayedTransition(root, autoTransition)
				updateHeaderSize(false, state, numExposureDays)
				headerBinding.apply {
					headerInfo.isVisible = true
					headerImage.isVisible = false
					headerContinueButton.isVisible = false
					headerShowAllButton.isVisible = state == State.POSSIBLE_INFECTION_TRACING_REPORTS && numExposureDays > 1
					headerDate.isVisible = state == State.POSSIBLE_INFECTION_TRACING_REPORTS
					headerSlogan.isVisible = state == State.POSSIBLE_INFECTION_CHECKIN_REPORT
					headerSubtitle.isVisible = state == State.POSSIBLE_INFECTION_TRACING_REPORTS
				}
			}
		}
	}

	private fun animateHeaderHeight(showAll: Boolean, numExposureDays: Int, exposureDaysContainer: View, dateTextView: View) {
		val exposureDayItemHeight = resources.getDimensionPixelSize(R.dimen.header_reports_exposure_day_height)
		val endExposureDayTopPadding: Int
		val endHeaderHeight: Int
		val endDateTextHeight: Int
		val endExposureDaysContainerHeight: Int
		val endScrollViewPadding: Int
		val screenHeight = Resources.getSystem().displayMetrics.heightPixels
		if (showAll) {
			endExposureDayTopPadding = resources.getDimensionPixelSize(R.dimen.spacing_medium)
			endHeaderHeight = min(
				screenHeight / 3 * 2,
				resources.getDimensionPixelSize(R.dimen.header_height_reports_multiple_days) + exposureDayItemHeight * (numExposureDays - 1) + endExposureDayTopPadding
			)
			endDateTextHeight = 0
			endExposureDaysContainerHeight =
				endHeaderHeight - resources.getDimensionPixelSize(R.dimen.header_height_reports_multiple_days) + exposureDayItemHeight
			endScrollViewPadding =
				endHeaderHeight - resources.getDimensionPixelSize(R.dimen.top_item_header_overlap_reports_multiple_days)
		} else {
			endExposureDayTopPadding = 0
			endHeaderHeight = resources.getDimensionPixelSize(R.dimen.header_height_reports_multiple_days)
			endDateTextHeight = exposureDayItemHeight
			endExposureDaysContainerHeight = 0
			endScrollViewPadding = resources.getDimensionPixelSize(R.dimen.top_item_padding_reports_multiple_days)
		}
		val startExposureDayTopPadding = exposureDaysContainer.paddingTop

		binding.apply {
			val startHeaderHeight = headerFragmentContainer.layoutParams.height
			val startScrollViewPadding = reportsScrollviewFirstChild.paddingTop
			val startDateTextHeight = dateTextView.layoutParams.height
			val startExposureDaysContainerHeight = exposureDaysContainer.layoutParams.height
			val anim = ValueAnimator.ofFloat(0f, 1f)
			anim.addUpdateListener { v: ValueAnimator ->
				val value = v.animatedValue as Float
				setHeight(headerFragmentContainer, value * (endHeaderHeight - startHeaderHeight) + startHeaderHeight)
				setHeight(dateTextView, value * (endDateTextHeight - startDateTextHeight) + startDateTextHeight)
				setHeight(
					exposureDaysContainer,
					value * (endExposureDaysContainerHeight - startExposureDaysContainerHeight) + startExposureDaysContainerHeight
				)
				reportsScrollviewFirstChild.setPadding(
					reportsScrollviewFirstChild.paddingLeft,
					(value * (endScrollViewPadding - startScrollViewPadding) + startScrollViewPadding).toInt(),
					reportsScrollviewFirstChild.paddingRight, reportsScrollviewFirstChild.paddingBottom
				)
				exposureDaysContainer.setPadding(
					exposureDaysContainer.paddingLeft,
					(value * (endExposureDayTopPadding - startExposureDayTopPadding) + startExposureDayTopPadding).toInt(),
					exposureDaysContainer.paddingRight, exposureDaysContainer.paddingBottom
				)
				if (value == 0f) {
					exposureDaysContainer.visibility = View.VISIBLE
					dateTextView.visibility = View.VISIBLE
				} else if (value == 1f) {
					if (showAll) {
						dateTextView.visibility = View.GONE
					} else {
						exposureDaysContainer.visibility = View.GONE
					}
					headerFragmentContainer.post { setupScrollBehavior() }
				}
			}
			anim.duration = 100
			anim.start()
		}
	}

	private fun setHeight(view: View, newHeight: Float) {
		view.updateLayoutParams {
			height = newHeight.toInt()
		}
	}

	private fun updateHeaderSize(isReportsHeaderAnimationPending: Boolean, state: State, numExposureDays: Int = 0) {
		binding.apply {
			val headerLp = headerFragmentContainer.layoutParams
			if (isReportsHeaderAnimationPending) {
				headerLp.height = ViewGroup.LayoutParams.MATCH_PARENT
			} else if (numExposureDays <= 1 || state != State.POSSIBLE_INFECTION_TRACING_REPORTS) {
				headerLp.height = resources.getDimensionPixelSize(R.dimen.header_height_reports)
				reportsScrollviewFirstChild.setPadding(
					reportsScrollviewFirstChild.paddingLeft,
					resources.getDimensionPixelSize(R.dimen.top_item_padding_reports),
					reportsScrollviewFirstChild.paddingRight, reportsScrollviewFirstChild.paddingBottom
				)
			} else {
				headerLp.height = resources.getDimensionPixelSize(R.dimen.header_height_reports_multiple_days)
				reportsScrollviewFirstChild.setPadding(
					reportsScrollviewFirstChild.paddingLeft,
					resources.getDimensionPixelSize(R.dimen.top_item_padding_reports_multiple_days),
					reportsScrollviewFirstChild.paddingRight, reportsScrollviewFirstChild.paddingBottom
				)
			}
			headerFragmentContainer.layoutParams = headerLp
			headerFragmentContainer.post { setupScrollBehavior() }
		}
	}

	private fun setupScrollBehavior() {
		if (!isVisible) return
		val rect = Rect()
		binding.apply {
			headerFragmentContainer.getDrawingRect(rect)
			reportsScrollview.setScrollPreventRect(rect)
			val scrollRangePx = reportsScrollviewFirstChild.paddingTop
			val translationRangePx = -resources.getDimensionPixelSize(R.dimen.spacing_huge)
			reportsScrollview.setOnScrollChangeListener { _, _, scrollY, _, _ ->
				val progress = computeScrollAnimProgress(scrollY, scrollRangePx)
				headerFragmentContainer.alpha = 1 - progress
				headerFragmentContainer.translationY = progress * translationRangePx
			}
			reportsScrollview.post {
				val progress = computeScrollAnimProgress(reportsScrollview.scrollY, scrollRangePx)
				headerFragmentContainer.alpha = 1 - progress
				headerFragmentContainer.translationY = progress * translationRangePx
			}
		}
	}

	private fun computeScrollAnimProgress(scrollY: Int, scrollRange: Int): Float {
		return min(scrollY, scrollRange) / scrollRange.toFloat()
	}

	enum class State(@LayoutRes val headerLayoutResource: Int) {
		NO_REPORTS(R.layout.fragment_reports_header_no_reports),
		POSSIBLE_INFECTION_CHECKIN_REPORT(R.layout.fragment_reports_header_possible_infection),
		POSSIBLE_INFECTION_TRACING_REPORTS(R.layout.fragment_reports_header_possible_infection),
		POSITIVE_TESTED(R.layout.fragment_reports_header_positive_tested)
	}

}