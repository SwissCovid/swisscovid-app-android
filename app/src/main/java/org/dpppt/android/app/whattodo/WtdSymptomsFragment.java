/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.whattodo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import org.dpppt.android.app.R;

public class WtdSymptomsFragment extends Fragment {

	public static WtdSymptomsFragment newInstance() {
		return new WtdSymptomsFragment();
	}

	public WtdSymptomsFragment() {
		super(R.layout.fragment_what_to_do_symptoms);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		Toolbar toolbar = view.findViewById(R.id.wtd_symptoms_toolbar);
		toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

		view.findViewById(R.id.wtd_symptoms_check_button).setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.symptom_detail_corona_check_url)));
			startActivity(browserIntent);
		});

		view.findViewById(R.id.wtd_symptoms_faq_button).setOnClickListener(v -> {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.faq_button_url)));
			startActivity(browserIntent);
		});
	}

}
