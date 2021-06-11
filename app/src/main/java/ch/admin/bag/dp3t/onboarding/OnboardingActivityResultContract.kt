package ch.admin.bag.dp3t.onboarding

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import ch.admin.bag.dp3t.onboarding.OnboardingActivity.Companion.ARG_INSTANT_APP_URL

class OnboardingActivityResultContract : ActivityResultContract<OnboardingActivityArgs, OnboardingActivityResult>() {

	override fun createIntent(context: Context, input: OnboardingActivityArgs): Intent {
		return Intent(context, OnboardingActivity::class.java).apply {
			putExtra(OnboardingActivity.ARG_ONBOARDING_TYPE, input.onboardingType)
			putExtra(ARG_INSTANT_APP_URL, input.instantAppUrl)
		}
	}

	override fun parseResult(resultCode: Int, intent: Intent?): OnboardingActivityResult {
		val onboardingType = intent?.getSerializableExtra(OnboardingActivity.ARG_ONBOARDING_TYPE) as OnboardingType
		val instantAppUrl = intent.getStringExtra(ARG_INSTANT_APP_URL)
		return OnboardingActivityResult(ActivityResult(resultCode, intent), onboardingType, instantAppUrl)
	}
}

data class OnboardingActivityArgs(val onboardingType: OnboardingType, val instantAppUrl: String? = null)

data class OnboardingActivityResult(
	val activityResult: ActivityResult,
	val onboardingType: OnboardingType,
	val instantAppUrl: String? = null
)