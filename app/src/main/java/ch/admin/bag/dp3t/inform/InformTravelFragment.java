package ch.admin.bag.dp3t.inform;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.travel.*;

public class InformTravelFragment extends Fragment {

	public static InformTravelFragment newInstance() {
		return new InformTravelFragment();
	}

	private ArrayList<String> selectedCountries = new ArrayList<>();
	private final String SELECTED_COUNTRIES_KEY = "SELECTED_COUNTRIES_KEY";

	public InformTravelFragment() {
		super(R.layout.fragment_inform_travel);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			selectedCountries = savedInstanceState.getStringArrayList(SELECTED_COUNTRIES_KEY);
			return;
		}
		SecureStorage secureStorage = SecureStorage.getInstance(getContext());
		for (Country country : secureStorage.getCountries()) {
			if (country.isActive()) selectedCountries.add(country.getIsoCode());
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

		Toolbar toolbar = view.findViewById(R.id.inform_travel_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		Button cancelButton = view.findViewById(R.id.inform_travel_cancel_button);
		cancelButton.setOnClickListener(v -> getActivity().finish());

		Button continueButton = view.findViewById(R.id.inform_travel_button_continue);
		continueButton.setOnClickListener(v -> {
			getParentFragmentManager()
					.beginTransaction()
					.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
					.replace(R.id.inform_fragment_container, InformFragment.newInstance())
					.addToBackStack(InformFragment.class.getCanonicalName())
					.commit();
			//TODO: Do something with selectedCountries!
		});

		TravelRecyclerAdapter adapter = new TravelRecyclerAdapter();
		RecyclerView recyclerView = view.findViewById(R.id.inform_travel_recyclerview);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);

		SecureStorage secureStorage = SecureStorage.getInstance(getContext());
		secureStorage.getCountriesLiveData().observe(getViewLifecycleOwner(), countries -> {
			ArrayList<TravelRecyclerItem> items = new ArrayList<>();
			items.add(
					new ItemIconAndText(R.string.travel_report_code_info, R.drawable.ic_travel, R.color.blue_main, R.color.white));

			String schweiz_display_name =
					new Locale("", "ch").getDisplayCountry(new Locale(requireContext().getString(R.string.language_key)));
			items.add(
					new ItemCountryWithCheckbox(schweiz_display_name, R.drawable.flag_ch, true, true, (v, isChecked) -> {},
							false));

			for (Country country : countries) {
				items.add(new ItemCountryWithCheckbox(
						country.getCountryName(getContext()),
						country.getFlagResId(requireContext()),
						selectedCountries.contains(country.getIsoCode()),
						false,
						(v, isChecked) -> {
							if (isChecked) selectedCountries.add(country.getIsoCode());
							else selectedCountries.remove(country.getIsoCode());
						},
						true
				));
			}

			adapter.setData(items);
		});
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putStringArrayList(SELECTED_COUNTRIES_KEY, selectedCountries);
		super.onSaveInstanceState(outState);
	}

}
