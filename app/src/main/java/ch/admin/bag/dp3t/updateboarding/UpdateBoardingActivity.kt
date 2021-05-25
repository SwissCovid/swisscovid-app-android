package ch.admin.bag.dp3t.updateboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import ch.admin.bag.dp3t.R
import org.dpppt.android.sdk.DP3T

class UpdateBoardingActivity : FragmentActivity() {


	companion object {
		// Increment this number for each new Update Boarding
		const val UPDATE_BOARDING_VERSION = 2
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_update_boarding)
		if (savedInstanceState == null) {
			showFirstUpdateBoardingFragment()
		}
	}

	private fun showFirstUpdateBoardingFragment() {
		// Replace with new UpdateBoarding Fragment
		supportFragmentManager
			.beginTransaction()
			.add(R.id.main_fragment_container, UpdateBoardingFragment.newInstance())
			.commit()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		DP3T.onActivityResult(this, requestCode, resultCode, data)
	}

	fun finishUpdateBoarding() {
		setResult(RESULT_OK)
		finish()
		overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
	}

}