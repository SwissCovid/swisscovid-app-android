package ch.admin.bag.dp3t.travel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.inform.InformActivity;

public class TravelFragment extends Fragment {

	public static TravelFragment newInstance() {
		return new TravelFragment();
	}

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

		ArrayList<TravelRecyclerItem> items = new ArrayList<>();

		items.add(new ItemIconAndText(R.string.travel_screen_introduction, R.drawable.ic_travel, R.color.blue_main,
				R.color.white));

		//TODO: Replace hardcoded Countries (PP-674)
		items.add(new ItemCountry("Österreich", -1, false, true, "Meldungen bis 21.09.2020", (a, b) -> {}));
		items.add(new ItemCountry("Deutschland", -1, false, false, null, (a, b) -> {}));
		items.add(new ItemCountry("Italien", -1, false, false, null, (a, b) -> {}));

		items.add(new ItemButton(R.string.travel_screen_add_countries_button, (v) -> {
			//TODO: Open Add Countries Fragment (PP-676)

		}));
		items.add(new ItemHeader(R.string.travel_screen_explanation_title_1));
		items.add(new ItemIconAndText(R.string.travel_screen_explanation_text_1, R.drawable.ic_begegnungen, R.color.blue_main,
				R.color.grey_light));
		items.add(new ItemHeader(R.string.travel_screen_explanation_title_2));
		items.add(new ItemIconAndText(R.string.travel_screen_explanation_text_2, R.drawable.ic_refresh, R.color.blue_main,
				R.color.grey_light));

		adapter.setData(items);
	}

}
