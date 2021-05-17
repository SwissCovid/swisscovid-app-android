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
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
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
import ch.admin.bag.dp3t.contacts.ReactivateTracingReminderDialog
import ch.admin.bag.dp3t.databinding.FragmentReportsBinding
import ch.admin.bag.dp3t.home.model.TracingStatusInterface
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.*
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import ch.admin.bag.dp3t.whattodo.WhereToTestDialogFragment
import org.dpppt.android.sdk.models.DayDate
import java.util.*
import kotlin.math.min

private const val DAYS_TO_STAY_IN_QUARANTINE = 10
private const val MAX_EXPOSURE_AGE_TO_DO_A_TEST = 10
private const val MIN_EXPOSURE_AGE_TO_DO_A_TEST = 5
private const val ONE_DAY_IN_MILLIS = 24L * 60 * 60 * 1000


class ReportsFragment : Fragment() {

	companion object {
		@JvmStatic
		fun newInstance() = ReportsFragment()
	}

	private val crowdNotifierViewModel: CrowdNotifierViewModel by activityViewModels()
	private val tracingViewModel: TracingViewModel by activityViewModels()
	private val secureStorage by lazy { SecureStorage.getInstance(requireContext()) }

	private lateinit var binding: FragmentReportsBinding

	private var leitfadenJustOpened = false

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentReportsBinding.inflate(inflater)
		return binding.apply {
			reportsToolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

			tracingViewModel.appStatusLiveData.observe(viewLifecycleOwner) {
				when {
					it.isReportedAsInfected -> setupState(State.POSITIVE_TESTED, it)
					it.wasContactReportedAsExposed() -> setupState(State.POSSIBLE_INFECTION, it)
					else -> setupState(State.NO_REPORTS, it)
				}
			}

		}.root
	}

	private fun setupState(state: State, tracingStatusInterface: TracingStatusInterface) {

		//TODO: Set up header
		binding.apply {
			reportsHealthy.root.isVisible = state == State.NO_REPORTS
			reportsInfected.root.isVisible = state == State.POSITIVE_TESTED
			reportsLeitfaden.root.isVisible = state == State.POSSIBLE_INFECTION
		}
		when (state) {
			State.NO_REPORTS -> {
				binding.reportsHealthy.apply {
					cardEncountersLink.setOnClickListener { openLink(R.string.no_meldungen_box_url) }
					faqButton.setOnClickListener { showFaq() }
				}
				setupHeaderFragment(ReportsHeaderFragment.Type.NO_REPORTS, 0);
			}
			State.POSSIBLE_INFECTION -> {
				binding.reportsLeitfaden.apply {
					itemCallHotlineLayout.setOnClickListener { callHotline() }
					faqButton.setOnClickListener { showFaq() }
					val isOpenLeitfadenPending = secureStorage.isOpenLeitfadenPending
					fillLeitfadenNowButton.isVisible = isOpenLeitfadenPending
					zumLeitfadenButton.isVisible = !isOpenLeitfadenPending
					xDaysLeftTextview.isVisible = !isOpenLeitfadenPending
					setupFreeTestInfoBox(tracingStatusInterface)
					if (isOpenLeitfadenPending) {
						fillLeitfadenNowButton.setOnClickListener { openSwissCovidLeitfaden() }
						leitfadenInfoButton.setOnClickListener { showLeitfadenInfo(fillLeitfadenNowButton.text.toString()) }
					} else {
						zumLeitfadenButton.setOnClickListener { openSwissCovidLeitfaden() }
						leitfadenInfoButton.setOnClickListener { showLeitfadenInfo(zumLeitfadenButton.text.toString()) }
					}

					val daysLeft = DAYS_TO_STAY_IN_QUARANTINE - tracingStatusInterface.daysSinceExposure.toInt()
					if (daysLeft > DAYS_TO_STAY_IN_QUARANTINE || daysLeft <= 0) {
						xDaysLeftTextview.isVisible = false
					} else if (daysLeft == 1) {
						xDaysLeftTextview.setText(R.string.date_in_one_day)
					} else {
						xDaysLeftTextview.text = getString(R.string.date_in_days).replace("{COUNT}", daysLeft.toString())
					}
					deleteReports.setOnClickListener { deleteNotifications(tracingStatusInterface) }
				}
				setupHeaderFragment(ReportsHeaderFragment.Type.POSSIBLE_INFECTION, tracingStatusInterface.exposureDays.size)

			}
			State.POSITIVE_TESTED -> {
				val oldestSharedKeyDateMillis = secureStorage.positiveReportOldestSharedKey
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
				setupHeaderFragment(ReportsHeaderFragment.Type.POSITIVE_TESTED, 0)
			}
		}
		val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(NotificationUtil.NOTIFICATION_ID_CONTACT)
	}

	private fun showDeleteReportConfirmationDialog() {
		AlertDialog.Builder(requireContext(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.delete_infection_dialog)
			.setPositiveButton(R.string.delete_infection_dialog_finish_button) { _, _ ->
				tracingViewModel.appStatusLiveData.value?.resetInfectionStatus(context)
				secureStorage.isolationEndDialogTimestamp = -1L
				secureStorage.positiveReportOldestSharedKey = -1L
				parentFragmentManager.popBackStack()
			}
			.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
			.create()
			.show()
	}

	private fun deleteNotifications(tracingStatusInterface: TracingStatusInterface) {
		AlertDialog.Builder(requireActivity(), R.style.NextStep_AlertDialogStyle)
			.setMessage(R.string.delete_notification_dialog)
			.setPositiveButton(R.string.delete_reports_button) { _, _ ->
				tracingStatusInterface.resetExposureDays(context)
				parentFragmentManager.popBackStack()
			}
			.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
			.create()
			.show()
	}

	private fun setupFreeTestInfoBox(tracingStatusInterface: TracingStatusInterface) {
		binding.reportsLeitfaden.viewFreeTestInfoBox.apply {
			testlocationsLink.setOnClickListener { showWhereToTestDialog() }
			val today = DayDate()
			if (tracingStatusInterface.exposureDays.filter {
					it.exposedDate.addDays(MIN_EXPOSURE_AGE_TO_DO_A_TEST).isBeforeOrEquals(today)
				}.find {
					!it.exposedDate.addDays(MAX_EXPOSURE_AGE_TO_DO_A_TEST).isBefore(today)
				} != null) {
				testCountdownTextview.setText(R.string.meldungen_detail_free_test_now)
				return
			}

			// This oldest exposure that is newer than MIN_EXPOSURE_AGE_TO_DO_A_TEST
			val oldestExposure = tracingStatusInterface.exposureDays.filter {
				!it.exposedDate.addDays(MIN_EXPOSURE_AGE_TO_DO_A_TEST).isBeforeOrEquals(today)
			}.minOf { it.exposedDate }

			testCountdownTextview.isVisible = oldestExposure != null

			if (oldestExposure != null) {
				val daysSinceFirstExposure =
					((today.startOfDayTimestamp - oldestExposure.startOfDayTimestamp) / ONE_DAY_IN_MILLIS).toInt()
				val daysUntilTest = MIN_EXPOSURE_AGE_TO_DO_A_TEST - daysSinceFirstExposure
				if (daysUntilTest == 1) {
					testCountdownTextview.setText(R.string.meldungen_detail_free_test_tomorrow)
				} else {
					testCountdownTextview.text = getString(R.string.meldungen_detail_free_test_in_x_tagen)
						.replace("{COUNT}", daysUntilTest.toString())
				}
			}
		}
	}

	private fun showWhereToTestDialog() {
		requireActivity().supportFragmentManager.beginTransaction()
			.add(WhereToTestDialogFragment.newInstance(), WhereToTestDialogFragment::class.java.canonicalName)
			.commit()
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

	private fun callHotline() {
		PhoneUtil.callHelpline(context)
	}

	override fun onResume() {
		super.onResume()
		if (leitfadenJustOpened) {
			leitfadenJustOpened = false
			setupState(State.POSSIBLE_INFECTION, tracingViewModel.tracingStatusInterface)
		}
	}

	fun doHeaderAnimation(info: View, image: View, button: Button, showAllButton: View, numExposureDays: Int) {
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
				updateHeaderSize(false, numExposureDays)
				info.visibility = View.VISIBLE
				image.visibility = View.GONE
				button.visibility = View.GONE
				if (numExposureDays <= 1) {
					showAllButton.visibility = View.GONE
				} else {
					showAllButton.visibility = View.VISIBLE
				}
			}
		}
	}

	fun animateHeaderHeight(showAll: Boolean, numExposureDays: Int, exposureDaysContainer: View, dateTextView: View) {
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

	private fun updateHeaderSize(isReportsHeaderAnimationPending: Boolean, numExposureDays: Int) {
		binding.apply {
			val headerLp = headerFragmentContainer.layoutParams
			if (isReportsHeaderAnimationPending) {
				headerLp.height = ViewGroup.LayoutParams.MATCH_PARENT
			} else if (numExposureDays <= 1) {
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

	private fun setupHeaderFragment(headerType: ReportsHeaderFragment.Type, numExposureDays: Int) {
		binding.apply {
			val isReportsHeaderAnimationPending = secureStorage.isReportsHeaderAnimationPending
			updateHeaderSize(isReportsHeaderAnimationPending, numExposureDays)
			if (isReportsHeaderAnimationPending) {
				reportsScrollviewFirstChild.visibility = View.GONE
			}
			headerFragmentContainer.post { setupScrollBehavior() }
			val header: Fragment = when (headerType) {
				ReportsHeaderFragment.Type.NO_REPORTS -> ReportsHeaderFragment.newInstance(
					ReportsHeaderFragment.Type.NO_REPORTS,
					false
				)
				ReportsHeaderFragment.Type.POSSIBLE_INFECTION -> ReportsHeaderFragment
					.newInstance(ReportsHeaderFragment.Type.POSSIBLE_INFECTION, isReportsHeaderAnimationPending)
				ReportsHeaderFragment.Type.POSITIVE_TESTED -> ReportsHeaderFragment.newInstance(
					ReportsHeaderFragment.Type.POSITIVE_TESTED,
					false
				)
			}
			childFragmentManager.beginTransaction()
				.replace(R.id.header_fragment_container, header)
				.commit()
		}
	}

	enum class State {
		NO_REPORTS, POSSIBLE_INFECTION, POSITIVE_TESTED
	}

}