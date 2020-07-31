package ch.admin.bag.dp3t.travel;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;

public class TravelFragment extends Fragment {

	public static TravelFragment newInstance() {
		return new TravelFragment();
	}

	public static int DAYS_TO_KEEP_NOTIFICATIONS_ACTIVE = 10;

	public TravelFragment() {
		super(R.layout.fragment_travel);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.travel_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
		TravelRecyclerAdapter adapter = new TravelRecyclerAdapter();

		RecyclerView recyclerView = view.findViewById(R.id.travel_screen_recycler_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);

		SecureStorage secureStorage = SecureStorage.getInstance(getContext());

		secureStorage.getCountriesLiveData().observe(getViewLifecycleOwner(), countries -> {

			ArrayList<TravelRecyclerItem> items = new ArrayList<>();
			items.add(new ItemSpace(R.dimen.spacing_larger, R.color.white));
			items.add(new ItemIconAndText(R.string.travel_screen_introduction, R.drawable.ic_travel, R.color.blue_main,
					R.color.white));

			Collections.sort(countries, (c1, c2) -> c1.getCountryName(getContext()).compareTo(c2.getCountryName(getContext())));

			boolean addTopSeparator = true;
			for (Country country : countries) {
				if (country.isFavourite()) {

					items.add(
							new ItemCountry(
									country.getCountryName(getContext()),
									country.getFlagResId(),
									country.isActive(),
									addTopSeparator,
									country.getStatusText(getContext(), DAYS_TO_KEEP_NOTIFICATIONS_ACTIVE),
									(v, isChecked) -> {
										if (isChecked == country.isActive()) return;
										country.setActive(isChecked);
										if (!isChecked) country.setDeactivationTimestamp(System.currentTimeMillis());
										else country.setDeactivationTimestamp(-1);
										secureStorage.setCountries(countries);
									}));

					addTopSeparator = false;
				}
			}

			items.add(new ItemButton(R.string.travel_screen_add_countries_button, (v) -> {
				getParentFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
						.replace(R.id.main_fragment_container, TravelAddCountriesFragment.newInstance())
						.addToBackStack(TravelAddCountriesFragment.class.getCanonicalName())
						.commit();
			}));
			items.add(new ItemHeader(R.string.travel_screen_explanation_title_1));
			items.add(new ItemIconAndText(R.string.travel_screen_explanation_text_1, R.drawable.ic_begegnungen, R.color.blue_main,
					R.color.grey_light));
			items.add(new ItemHeader(R.string.travel_screen_explanation_title_2));
			items.add(new ItemIconAndText(R.string.travel_screen_explanation_text_2, R.drawable.ic_refresh, R.color.blue_main,
					R.color.grey_light));

			adapter.setData(items);
		});
	}

}
