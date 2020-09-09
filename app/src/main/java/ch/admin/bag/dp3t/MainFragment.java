package ch.admin.bag.dp3t;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.concurrent.atomic.AtomicLong;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ch.admin.bag.dp3t.debug.DebugFragment;
import ch.admin.bag.dp3t.home.HomeFragment;
import ch.admin.bag.dp3t.html.HtmlFragment;
import ch.admin.bag.dp3t.stats.StatsFragment;
import ch.admin.bag.dp3t.util.AssetUtil;

public class MainFragment extends Fragment {

	private Toolbar toolbar;
	private View schwiizerchruez;
	private BottomNavigationView bottomNavigationView;

	private int lastSelectedTab = R.id.bottom_nav_home;

	public static MainFragment newInstance() {
		return new MainFragment();
	}

	private MainFragment() {
		super(R.layout.fragment_main);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		toolbar = view.findViewById(R.id.home_toolbar);
		schwiizerchruez = view.findViewById(R.id.schwiizerchruez);
		bottomNavigationView = view.findViewById(R.id.fragment_main_navigation_view);

		setupToolbar();
		setupDebugButton();
		setupBottomNavigationView();

		bottomNavigationView.setSelectedItemId(lastSelectedTab);
	}

	private void setupToolbar() {
		toolbar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.homescreen_menu_impressum) {
				HtmlFragment htmlFragment =
						HtmlFragment.newInstance(R.string.menu_impressum, AssetUtil.getImpressumBaseUrl(getContext()),
								AssetUtil.getImpressumHtml(getContext()));
				getParentFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.root_fragment_container, htmlFragment)
						.addToBackStack(HtmlFragment.class.getCanonicalName())
						.commit();
				return true;
			}
			return false;
		});
	}

	private void setupDebugButton() {
		if (!DebugFragment.EXISTS) {
			return;
		}

		AtomicLong lastClick = new AtomicLong(0);
		schwiizerchruez.setOnClickListener(v -> {
			if (lastClick.get() > System.currentTimeMillis() - 1000L) {
				lastClick.set(0);
				DebugFragment.startDebugFragment(getParentFragmentManager());
			} else {
				lastClick.set(System.currentTimeMillis());
			}
		});
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

//	public void showBehaviour() {
//		navigationView.setSelectedItemId(R.id.action_verhalten);
//	}
}
