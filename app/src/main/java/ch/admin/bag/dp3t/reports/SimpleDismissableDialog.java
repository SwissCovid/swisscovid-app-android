package ch.admin.bag.dp3t.reports;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import ch.admin.bag.dp3t.R;

public class SimpleDismissableDialog extends DialogFragment {

	private static final String ARG_TITLE = "ARG_TINT_COLOR";
	private static final String ARG_SUBTITLE = "ARG_TITLE";

	public static SimpleDismissableDialog newInstance(String title, String subtitle) {
		SimpleDismissableDialog simpleDismissableDialog = new SimpleDismissableDialog();
		Bundle arguments = new Bundle();
		arguments.putString(ARG_TITLE, title);
		arguments.putString(ARG_SUBTITLE, subtitle);
		simpleDismissableDialog.setArguments(arguments);
		return simpleDismissableDialog;
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
		return inflater.inflate(R.layout.dialog_simple_dismissable, container);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		TextView titleTextView = view.findViewById(R.id.title_textview);
		TextView subtitleTextView = view.findViewById(R.id.subtitle_textview);
		ImageView closeButton = view.findViewById(R.id.close_button);

		titleTextView.setText(getArguments().getString(ARG_TITLE));
		subtitleTextView.setText(getArguments().getString(ARG_SUBTITLE));

		closeButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(view.getContext(), R.color.blue_main)));
		closeButton.setOnClickListener(v -> dismiss());
	}

}
