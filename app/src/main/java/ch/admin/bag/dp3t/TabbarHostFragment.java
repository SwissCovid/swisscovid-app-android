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
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ch.admin.bag.dp3t.home.HomeFragment;
import ch.admin.bag.dp3t.stats.StatsFragment;
import ch.admin.bag.dp3t.util.ToolbarUtil;

public class TabbarHostFragment extends Fragment {

	private BottomNavigationView bottomNavigationView;

	private int lastSelectedTab = -1;

	public static TabbarHostFragment newInstance() {
		return new TabbarHostFragment();
	}

	private TabbarHostFragment() {
		super(R.layout.fragment_tabbar_host);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ToolbarUtil.setupToolbar(getContext(), view.findViewById(R.id.main_toolbar), getActivity().getSupportFragmentManager());

		bottomNavigationView = view.findViewById(R.id.fragment_main_navigation_view);

		setupBottomNavigationView();

		if (lastSelectedTab == -1) {
			bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
		}
	}

	private void setupBottomNavigationView() {
		bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
			lastSelectedTab = item.getItemId();

			switch (item.getItemId()) {
				case R.id.bottom_nav_home:
					getChildFragmentManager().beginTransaction()
							.replace(R.id.tabs_fragment_container, HomeFragment.newInstance())
							.commit();
					break;
				case R.id.bottom_nav_stats:
					getChildFragmentManager().beginTransaction()
							.replace(R.id.tabs_fragment_container, StatsFragment.newInstance())
							.commit();
					break;
			}
			return true;
		});
	}

}
