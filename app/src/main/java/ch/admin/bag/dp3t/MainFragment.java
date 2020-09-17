package ch.admin.bag.dp3t;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ch.admin.bag.dp3t.home.HomeFragment;
import ch.admin.bag.dp3t.stats.StatsFragment;

public class MainFragment extends Fragment {

	private BottomNavigationView bottomNavigationView;

	private int lastSelectedTab = R.id.bottom_nav_home;

	public static MainFragment newInstance() {
		return new MainFragment();
	}

	private MainFragment() {
		super(R.layout.fragment_main);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		bottomNavigationView = view.findViewById(R.id.fragment_main_navigation_view);

		setupBottomNavigationView();

		bottomNavigationView.setSelectedItemId(lastSelectedTab);
	}

	private void setupBottomNavigationView() {
		bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
			lastSelectedTab = item.getItemId();

			switch (item.getItemId()) {
				case R.id.bottom_nav_home:
					getParentFragmentManager().beginTransaction()
							.replace(R.id.tabs_fragment_container, HomeFragment.newInstance())
							.commit();
					break;
				case R.id.bottom_nav_stats:
					getParentFragmentManager().beginTransaction()
							.replace(R.id.tabs_fragment_container, StatsFragment.newInstance())
							.commit();
					break;
			}
			return true;
		});
	}

}
