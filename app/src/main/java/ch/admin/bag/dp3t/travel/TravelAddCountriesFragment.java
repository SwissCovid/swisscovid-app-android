package ch.admin.bag.dp3t.travel;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;

public class TravelAddCountriesFragment extends Fragment {

	public static TravelAddCountriesFragment newInstance() {
		return new TravelAddCountriesFragment();
	}

	public TravelAddCountriesFragment() {
		super(R.layout.fragment_travel);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.travel_toolbar);
		toolbar.setTitle(R.string.travel_screen_add_countries_button);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
		TravelRecyclerAdapter adapter = new TravelRecyclerAdapter();

		RecyclerView recyclerView = view.findViewById(R.id.travel_screen_recycler_view);
		recyclerView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_light));
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);

		SecureStorage secureStorage = SecureStorage.getInstance(getContext());

		secureStorage.getCountriesLiveData().observe(getViewLifecycleOwner(), countries -> {
			ArrayList<TravelRecyclerItem> items = new ArrayList<>();

			items.add(new ItemAllCapsHeader(R.string.travel_screen_favourites));

			for (Country country : countries) {
				if (country.isFavourite()) {
					items.add(new ItemEditableCountry(
							country.getCountryName(getContext()),
							country.getFlagResId(),
							R.drawable.ic_remove,
							true,
							v -> {
								country.setFavourite(false);
								country.setActive(false);
								country.setDeactivationTimestamp(-1);
								secureStorage.setCountries(countries);
							}));
				}
			}

			items.add(new ItemAllCapsHeader(R.string.travel_screen_other_countries));

			for (Country country : countries) {
				if (!country.isFavourite()) {
					items.add(new ItemEditableCountry(
							country.getCountryName(getContext()),
							country.getFlagResId(),
							R.drawable.ic_add,
							false,
							v -> {
								country.setFavourite(true);
								country.setDeactivationTimestamp(-1);
								country.setActive(false);
								secureStorage.setCountries(countries);
							}));
				}
			}

			items.add(new ItemSpace(R.dimen.travel_button_bottom_margin, R.color.grey_light));
			items.add(new ItemHeader(R.string.travel_screen_add_countries_explanation_title));
			items.add(new ItemIconAndText(R.string.travel_screen_add_countries_explanation_text, R.drawable.ic_info,
					R.color.blue_main, R.color.grey_light));
			adapter.setData(items);
		});
	}

}
