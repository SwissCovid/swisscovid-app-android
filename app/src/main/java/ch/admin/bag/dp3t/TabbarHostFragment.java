/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.atomic.AtomicLong;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ch.admin.bag.dp3t.debug.DebugFragment;
import ch.admin.bag.dp3t.home.HomeFragment;
import ch.admin.bag.dp3t.html.HtmlFragment;
import ch.admin.bag.dp3t.infotab.InfoTabFragment;
import ch.admin.bag.dp3t.stats.StatsFragment;
import ch.admin.bag.dp3t.stats.StatsViewModel;
import ch.admin.bag.dp3t.util.AssetUtil;

public class TabbarHostFragment extends Fragment {

	private static final long MAX_DURATION_TO_STAY_AWAY_FROM_HOME_TAB = 60 * 60 * 1000L; //1h

	private StatsViewModel statsViewModel;
	private BottomNavigationView bottomNavigationView;

	private int lastSelectedTab = -1;
	private long lastTabSwitch = 0;

	public static TabbarHostFragment newInstance() {
		return new TabbarHostFragment();
	}

	public TabbarHostFragment() {
		super(R.layout.fragment_tabbar_host);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		statsViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Toolbar toolbar = view.findViewById(R.id.main_toolbar);
		toolbar.setOnMenuItemClickListener(item -> {
			if (item.getItemId() == R.id.homescreen_menu_impressum) {
				HtmlFragment htmlFragment =
						HtmlFragment.newInstance(R.string.menu_impressum, AssetUtil.getImpressumBaseUrl(getContext()),
								AssetUtil.getImpressumHtml(getContext()));
				getActivity().getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.main_fragment_container, htmlFragment)
						.addToBackStack(HtmlFragment.class.getCanonicalName())
						.commit();
				return true;
			}
			return false;
		});

		View schwiizerchruez = toolbar.findViewById(R.id.schwiizerchruez);
		setupDebugButton(schwiizerchruez, getActivity().getSupportFragmentManager());

		bottomNavigationView = view.findViewById(R.id.fragment_main_navigation_view);

		setupBottomNavigationView();
	}

	private static void setupDebugButton(View schwiizerchruez, FragmentManager fragmentManager) {
		if (!DebugFragment.EXISTS) {
			return;
		}

		AtomicLong lastClick = new AtomicLong(0);
		schwiizerchruez.setOnClickListener(v -> {
			if (lastClick.get() > System.currentTimeMillis() - 1000L) {
				lastClick.set(0);
				DebugFragment.startDebugFragment(fragmentManager);
			} else {
				lastClick.set(System.currentTimeMillis());
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		if (lastSelectedTab == -1 || lastTabSwitch < System.currentTimeMillis() - MAX_DURATION_TO_STAY_AWAY_FROM_HOME_TAB) {
			bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
		}
	}

	private void setupBottomNavigationView() {
		bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
			lastSelectedTab = item.getItemId();
			lastTabSwitch = System.currentTimeMillis();

			switch (item.getItemId()) {
				case R.id.bottom_nav_stats:
					getChildFragmentManager().beginTransaction()
							.replace(R.id.tabs_fragment_container, StatsFragment.newInstance())
							.commit();

					statsViewModel.loadStats();
					break;
				case R.id.bottom_nav_info:
					getChildFragmentManager().beginTransaction()
							.replace(R.id.tabs_fragment_container, InfoTabFragment.newInstance())
							.commit();
					break;
				default:
					getChildFragmentManager().beginTransaction()
							.replace(R.id.tabs_fragment_container, HomeFragment.newInstance())
							.commit();
					break;
			}
			return true;
		});
	}

}
