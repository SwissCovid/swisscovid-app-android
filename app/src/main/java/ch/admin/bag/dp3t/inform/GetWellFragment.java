/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package ch.admin.bag.dp3t.inform;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.MainActivity;
import ch.admin.bag.dp3t.R;

public class GetWellFragment extends Fragment {

	public static GetWellFragment newInstance() {
		return new GetWellFragment();
	}

	public GetWellFragment() {
		super(R.layout.fragment_get_well);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((InformActivity) requireActivity()).allowBackButton(false);

		view.findViewById(R.id.inform_get_well_button_continue).setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), MainActivity.class);
			intent.setAction(MainActivity.ACTION_STOP_TRACING);
			startActivity(intent);
			getActivity().finish();
		});
	}

}
