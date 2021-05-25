package ch.admin.bag.dp3t.checkin.diary;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.crowdnotifier.android.sdk.CrowdNotifier;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.checkin.models.DiaryEntry;
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage;

public class HideInDiaryDialogFragment extends DialogFragment {

	public static final String TAG = HideInDiaryDialogFragment.class.getCanonicalName();
	private static final String ARG_DIARY_ENTRY_ID = "ARG_DIARY_ENTRY_ID";

	private DiaryStorage diaryStorage;
	private DiaryEntry diaryEntry;

	public static HideInDiaryDialogFragment newInstance(long entryId) {
		HideInDiaryDialogFragment fragment = new HideInDiaryDialogFragment();
		Bundle args = new Bundle();
		args.putLong(ARG_DIARY_ENTRY_ID, entryId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		diaryStorage = DiaryStorage.getInstance(getContext());
		diaryEntry = diaryStorage.getDiaryEntryWithId(getArguments().getLong(ARG_DIARY_ENTRY_ID));
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_fragment_hide_in_diary, container);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		View closeButton = view.findViewById(R.id.checkin_remove_close_button);
		View hideButton = view.findViewById(R.id.checkin_remove_hide_button);
		TextView nukeButton = view.findViewById(R.id.checkin_remove_nuke_button);

		nukeButton.setPaintFlags(nukeButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

		closeButton.setOnClickListener(v -> dismiss());
		hideButton.setOnClickListener(v -> hideNow());
		nukeButton.setOnClickListener(v -> nukeNow());
	}

	@Override
	public void onResume() {
		getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		super.onResume();
	}

	private void hideNow() {
		diaryStorage.removeEntry(diaryEntry.getId());
		dismiss();
		requireActivity().getSupportFragmentManager().popBackStack();
	}

	private void nukeNow() {
		diaryStorage.removeEntry(diaryEntry.getId());
		CrowdNotifier.deleteCheckIn(diaryEntry.getId(), requireContext());
		dismiss();
		requireActivity().getSupportFragmentManager().popBackStack();
	}

}
