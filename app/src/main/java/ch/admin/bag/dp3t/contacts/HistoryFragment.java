package ch.admin.bag.dp3t.contacts;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.viewmodel.TracingViewModel;

public class HistoryFragment extends Fragment {

	private TracingViewModel tracingViewModel;

	private View loadingView;
	private RecyclerView recyclerView;
	private HistoryAdapter historyAdapter;

	public static HistoryFragment newInstance() {
		return new HistoryFragment();
	}

	public HistoryFragment() {
		super(R.layout.fragment_history);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tracingViewModel = new ViewModelProvider(requireActivity()).get(TracingViewModel.class);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.history_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		setupRecyclerView(view);
	}

	private void setupRecyclerView(View view) {
		loadingView = view.findViewById(R.id.loading_view);
		loadingView.setVisibility(View.VISIBLE);

		recyclerView = view.findViewById(R.id.history_recycler_view);
		recyclerView.setHasFixedSize(true);
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		recyclerView.setLayoutManager(layoutManager);
		historyAdapter = new HistoryAdapter();
		recyclerView.setAdapter(historyAdapter);

		tracingViewModel.getHistoryLiveDate().observe(getViewLifecycleOwner(), historyEntries -> {
			if (historyEntries != null) {
				historyAdapter.setItems(historyEntries);
				loadingView.animate()
						.alpha(0f)
						.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
						.withEndAction(() -> loadingView.setVisibility(View.GONE))
						.start();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		tracingViewModel.loadHistoryEntries();
		if (tracingViewModel.getHistoryLiveDate().getValue() == null) {
			loadingView.setVisibility(View.VISIBLE);
		}
	}

}
