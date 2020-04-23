/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.inform;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.MainActivity;
import org.dpppt.android.app.R;

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

		view.findViewById(R.id.inform_get_well_button_continue).setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), MainActivity.class);
			startActivity(intent);
			getActivity().finish();
		});
	}



}
