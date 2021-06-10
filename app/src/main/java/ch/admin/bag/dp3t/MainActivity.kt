/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t

import android.content.*
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ch.admin.bag.dp3t.checkin.CheckinOverviewFragment
import ch.admin.bag.dp3t.checkin.CrowdNotifierViewModel
import ch.admin.bag.dp3t.checkin.checkinflow.CheckInFragment
import ch.admin.bag.dp3t.checkin.checkinflow.CheckOutFragment
import ch.admin.bag.dp3t.checkin.models.CheckInState
import ch.admin.bag.dp3t.checkin.models.CrowdNotifierErrorState
import ch.admin.bag.dp3t.checkin.networking.CrowdNotifierKeyLoadWorker
import ch.admin.bag.dp3t.checkin.utils.CrowdNotifierReminderHelper
import ch.admin.bag.dp3t.checkin.utils.ErrorDialog
import ch.admin.bag.dp3t.checkin.utils.NotificationHelper
import ch.admin.bag.dp3t.inform.InformActivity
import ch.admin.bag.dp3t.networking.ConfigWorker.Companion.scheduleConfigWorkerIfOutdated
import ch.admin.bag.dp3t.onboarding.OnboardingActivityArgs
import ch.admin.bag.dp3t.onboarding.OnboardingActivityResultContract
import ch.admin.bag.dp3t.onboarding.OnboardingSlidePageAdapter.Companion.UPDATE_BOARDING_VERSION
import ch.admin.bag.dp3t.onboarding.OnboardingType
import ch.admin.bag.dp3t.reports.ReportsFragment
import ch.admin.bag.dp3t.reports.ReportsOverviewFragment
import ch.admin.bag.dp3t.storage.SecureStorage
import ch.admin.bag.dp3t.util.NotificationUtil
import ch.admin.bag.dp3t.util.UrlUtil
import ch.admin.bag.dp3t.viewmodel.TracingViewModel
import ch.admin.bag.dp3t.whattodo.WtdPositiveTestFragment
import com.google.android.gms.instantapps.InstantApps
import org.crowdnotifier.android.sdk.CrowdNotifier
import org.crowdnotifier.android.sdk.utils.QrUtils.*
import org.dpppt.android.sdk.DP3T
import java.nio.charset.StandardCharsets


class MainActivity : FragmentActivity() {


	companion object {
		const val ACTION_EXPOSED_GOTO_REPORTS = "ACTION_EXPOSED_GOTO_REPORTS"
		const val ACTION_INFORMED_GOTO_REPORTS = "ACTION_INFORMED_GOTO_REPORTS"
		private const val KEY_IS_INTENT_CONSUMED = "KEY_IS_INTENT_CONSUMED"
	}

	private var isIntentConsumed = false
	private val secureStorage: SecureStorage by lazy { SecureStorage.getInstance(this) }
	private val tracingViewModel: TracingViewModel by viewModels()
	private val crowdNotifierViewModel: CrowdNotifierViewModel by viewModels()

	private val onboardingLauncher = registerForActivityResult(OnboardingActivityResultContract()) {
		onOnboardingFinished(it.onboardingType, it.activityResult, it.instantAppUrl)
	}

	private val autoCheckoutBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			showHomeFragment()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		secureStorage.forceUpdateLiveData.observe(this, {

			val forceUpdate = it && secureStorage.doForceUpdate

			if (forceUpdate) {
				val forceUpdateDialog = AlertDialog.Builder(this, R.style.NextStep_AlertDialogStyle)
					.setTitle(R.string.force_update_title)
					.setMessage(R.string.force_update_text)
					.setPositiveButton(R.string.playservices_update, null)
					.setCancelable(false)
					.create()
				forceUpdateDialog.setOnShowListener {
					forceUpdateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
						UrlUtil.openUrl(this@MainActivity, "market://details?id=$packageName")
					}
				}
				forceUpdateDialog.show()
			}
		})
		scheduleConfigWorkerIfOutdated(this)
		CrowdNotifierKeyLoadWorker.startKeyLoadWorker(this)
		CrowdNotifierKeyLoadWorker.cleanUpOldData(this)
		if (savedInstanceState == null) {
			val onboardingCompleted = secureStorage.onboardingCompleted
			val lastShownUpdateBoardingVersion = secureStorage.lastShownUpdateBoardingVersion
			val instantAppQrCodeUrl = checkForInstantAppUrl()

			val onboardingType = when {
				instantAppQrCodeUrl != null -> OnboardingType.INSTANT_PART
				!onboardingCompleted -> OnboardingType.NORMAL
				lastShownUpdateBoardingVersion < UPDATE_BOARDING_VERSION -> OnboardingType.UPDATE_BOARDING
				else -> null
			}

			if (onboardingType == null) {
				showHomeFragment()
			} else {
				launchOnboarding(onboardingType, instantAppQrCodeUrl)
			}
		} else {
			isIntentConsumed = savedInstanceState.getBoolean(KEY_IS_INTENT_CONSUMED)
		}
		tracingViewModel.sync()
	}

	fun launchOnboarding(onboardingType: OnboardingType, instantAppQrCodeUrl: String? = null) {
		onboardingLauncher.launch(OnboardingActivityArgs(onboardingType, instantAppQrCodeUrl))
	}

	private fun onOnboardingFinished(onboardingType: OnboardingType, activityResult: ActivityResult, qrCodeUrl: String? = null) {

		if (activityResult.resultCode == RESULT_OK) {
			secureStorage.lastShownUpdateBoardingVersion = UPDATE_BOARDING_VERSION
			secureStorage.onboardingCompleted = true
			if (onboardingType == OnboardingType.INSTANT_PART) {
				secureStorage.onlyPartialOnboardingCompleted = true
				qrCodeUrl?.let { checkIn(it) }
			} else {
				secureStorage.onlyPartialOnboardingCompleted = false
			}
			showHomeFragment()
		} else {
			finish()
		}
	}

	private fun checkForInstantAppUrl(): String? {
		val pmc = InstantApps.getPackageManagerCompat(this)
		val instantAppCookie = pmc.instantAppCookie
		if (instantAppCookie != null && instantAppCookie.isNotEmpty()) {
			// If there is an url in the instant app cookies, retun it and reset it to null
			val url = String(instantAppCookie, StandardCharsets.UTF_8)
			pmc.instantAppCookie = null
			return url
		}
		return null
	}

	public override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putBoolean(KEY_IS_INTENT_CONSUMED, isIntentConsumed)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		setIntent(intent)
		isIntentConsumed = false
	}

	override fun onStart() {
		super.onStart()
		secureStorage.appOpenAfterNotificationPending = false
		CrowdNotifierReminderHelper.autoCheckoutIfNecessary(this, crowdNotifierViewModel.checkInState)
	}

	override fun onResume() {
		super.onResume()
		if (secureStorage.onboardingCompleted) checkIntentForActions()
		LocalBroadcastManager.getInstance(this)
			.registerReceiver(autoCheckoutBroadcastReceiver, IntentFilter(CrowdNotifierReminderHelper.ACTION_DID_AUTO_CHECKOUT))
	}

	private fun checkIntentForActions() {
		val intent = intent
		val launchedFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
		if (!launchedFromHistory && !isIntentConsumed) {
			isIntentConsumed = true
			handleCustomIntents()
		}
	}

	private fun handleCustomIntents() {
		val intentAction = intent.action
		val isCheckedIn = crowdNotifierViewModel.isCheckedIn.value ?: false
		val hasCheckinExposures = crowdNotifierViewModel.exposures.value?.isNotEmpty() == true
		if (ACTION_INFORMED_GOTO_REPORTS == intentAction) {
			secureStorage.setLeitfadenOpenPending(false)
			secureStorage.isReportsHeaderAnimationPending = false
			showReportsFragment()
		} else if (ACTION_EXPOSED_GOTO_REPORTS == intentAction) {
			if (tracingViewModel.tracingStatusInterface.wasContactReportedAsExposed() || hasCheckinExposures) {
				showReportsFragment()
			}
		} else if (NotificationUtil.ACTION_ACTIVATE_TRACING == intentAction) {
			tracingViewModel.enableTracing(this, {}, { }) {}
		} else if ((NotificationHelper.ACTION_CROWDNOTIFIER_REMINDER_NOTIFICATION == intentAction || NotificationHelper.ACTION_ONGOING_NOTIFICATION == intentAction) && isCheckedIn) {
			showFragmentWithoutAnimation(CheckinOverviewFragment.newInstance())
		} else if (NotificationHelper.ACTION_CHECK_OUT_NOW == intentAction && isCheckedIn) {
			showCheckOutFragment()
		} else if (intent.data != null) {
			checkValidCovidcodeIntent()
			checkValidCheckInIntent()
		}
	}

	private fun checkValidCheckInIntent() {
		val qrCodeData = intent.dataString ?: return

		if (Uri.parse(qrCodeData).host != BuildConfig.ENTRY_QR_CODE_HOST) return
		try {
			val venueInfo = CrowdNotifier.getVenueInfo(qrCodeData, BuildConfig.ENTRY_QR_CODE_HOST)
			if (crowdNotifierViewModel.isCheckedIn.value == true) {
				ErrorDialog(this, CrowdNotifierErrorState.ALREADY_CHECKED_IN).show()
			} else {
				crowdNotifierViewModel.checkInState = CheckInState(
					false, venueInfo, System.currentTimeMillis(),
					System.currentTimeMillis(), 0
				)
				showFragmentWithoutAnimation(CheckInFragment.newInstance(false))
			}
		} catch (e: QRException) {
			handleInvalidQRCodeExceptions(e)
		}
	}

	private fun checkIn(qrCodeUrl: String) {
		try {
			val venueInfo = CrowdNotifier.getVenueInfo(qrCodeUrl, BuildConfig.ENTRY_QR_CODE_HOST)
			if (crowdNotifierViewModel.isCheckedIn.value == true) {
				ErrorDialog(this, CrowdNotifierErrorState.ALREADY_CHECKED_IN).show()
			} else {
				crowdNotifierViewModel.performCheckinAndSetReminders(venueInfo, 0)
			}
		} catch (e: QRException) {
			handleInvalidQRCodeExceptions(e)
		}
	}

	private fun handleInvalidQRCodeExceptions(e: QRException) {
		if (e is InvalidQRCodeVersionException) {
			ErrorDialog(this, CrowdNotifierErrorState.UPDATE_REQUIRED).show()
		} else if (e is NotYetValidException) {
			ErrorDialog(this, CrowdNotifierErrorState.QR_CODE_NOT_YET_VALID).show()
		} else if (e is NotValidAnymoreException) {
			ErrorDialog(this, CrowdNotifierErrorState.QR_CODE_NOT_VALID_ANYMORE).show()
		} else {
			ErrorDialog(this, CrowdNotifierErrorState.NO_VALID_QR_CODE).show()
		}
	}

	private fun checkValidCovidcodeIntent() {
		val tracingStatus = tracingViewModel.appStatusLiveData.value
		if (tracingStatus == null || tracingStatus.isReportedAsInfected) {
			return
		}
		val uri = Uri.parse(intent.data.toString())
		if (uri.host != "cc.admin.ch") return
		if (uri.path != "" && uri.path != "/") return
		val covidCode = uri.fragment
		if (covidCode == null || covidCode.length != 12) return
		startInformFlow(covidCode)
	}

	private fun startInformFlow(covidCode: String) {
		showFragmentWithoutAnimation(WtdPositiveTestFragment.newInstance())
		val intent = Intent(this, InformActivity::class.java)
		intent.putExtra(InformActivity.EXTRA_COVIDCODE, covidCode)
		startActivity(intent)
	}

	private fun showHomeFragment() {
		supportFragmentManager.beginTransaction()
			.add(R.id.main_fragment_container, TabbarHostFragment.newInstance())
			.commit()
	}

	private fun showReportsFragment() {
		val checkinReports = crowdNotifierViewModel.exposures.value?.size ?: 0
		val tracingReports = tracingViewModel.appStatusLiveData.value?.exposureDays?.size ?: 0
		val isReportedPositive = tracingViewModel.tracingStatusInterface.isReportedAsInfected
		val reportsFragment: Fragment =
			if ((checkinReports > 0 && tracingReports > 0 || checkinReports > 1) && !isReportedPositive) {
				ReportsOverviewFragment.newInstance()
			} else {
				ReportsFragment.newInstance(null)
			}
		showFragmentWithoutAnimation(reportsFragment)
	}

	private fun showCheckOutFragment() {
		supportFragmentManager.beginTransaction()
			.setCustomAnimations(R.anim.modal_slide_enter, R.anim.modal_slide_exit, R.anim.modal_pop_enter, R.anim.modal_pop_exit)
			.replace(R.id.main_fragment_container, CheckOutFragment.newInstance())
			.addToBackStack(CheckOutFragment::class.java.canonicalName)
			.commit()
	}

	private fun showFragmentWithoutAnimation(fragment: Fragment) {
		supportFragmentManager.beginTransaction()
			.replace(R.id.main_fragment_container, fragment)
			.addToBackStack(fragment::class.java.canonicalName)
			.commit()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		DP3T.onActivityResult(this, requestCode, resultCode, data)
	}

	override fun onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(autoCheckoutBroadcastReceiver)
		super.onPause()
	}

}