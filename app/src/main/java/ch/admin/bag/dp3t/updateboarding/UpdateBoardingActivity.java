package ch.admin.bag.dp3t.updateboarding;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import org.dpppt.android.sdk.DP3T;

import ch.admin.bag.dp3t.R;

public class UpdateBoardingActivity extends FragmentActivity {

	// Increment this number for each new Update Boarding
	public static final int UPDATE_BOARDING_VERSION = 1;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_boarding);
		if (savedInstanceState == null) {
			showFirstUpdateBoardingFragment();
		}
	}

	private void showFirstUpdateBoardingFragment() {
		// Replace with new UpdateBoarding Fragment
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.main_fragment_container, InteroperabilityUpdateBoardingFragment.newInstance())
				.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DP3T.onActivityResult(this, requestCode, resultCode, data);
	}

	public void finishUpdateBoarding() {
		setResult(RESULT_OK);
		finish();
		overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_open_exit);
	}

}
