package ch.admin.bag.dp3t

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class InstantActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_instant)

		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.instant_fragment_container, InstantFragment.newInstance())
				.commit()
		}
	}

}