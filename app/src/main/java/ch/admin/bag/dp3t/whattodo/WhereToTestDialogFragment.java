package ch.admin.bag.dp3t.whattodo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Map;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.storage.SecureStorage;
import ch.admin.bag.dp3t.util.UrlUtil;

public class WhereToTestDialogFragment extends DialogFragment {

	public static WhereToTestDialogFragment newInstance() { return new WhereToTestDialogFragment(); }

	private SecureStorage secureStorage;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		secureStorage = SecureStorage.getInstance(requireContext());
	}

	@Override
	public void onResume() {
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		super.onResume();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_where_to_do_test, container);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		view.findViewById(R.id.where_to_test_close_button).setOnClickListener(v -> dismiss());
		ViewGroup cantonsContainer = view.findViewById(R.id.where_to_test_links_container);
		cantonsContainer.removeAllViews();
		Map<String, String> cantonUrls = secureStorage.getTestLocations();
		for (Map.Entry<String, String> cantonEntry : cantonUrls.entrySet()) {
			TextView cantonView = (TextView) View.inflate(cantonsContainer.getContext(), R.layout.item_external_link, null);
			cantonView.setText(getResources().getIdentifier(cantonEntry.getKey(), "string", requireContext().getPackageName()));
			cantonView.setOnClickListener(v -> UrlUtil.openUrl(requireContext(), cantonEntry.getValue()));
			cantonView.setLayoutParams(
					new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			cantonsContainer.addView(cantonView);
		}
	}

}
