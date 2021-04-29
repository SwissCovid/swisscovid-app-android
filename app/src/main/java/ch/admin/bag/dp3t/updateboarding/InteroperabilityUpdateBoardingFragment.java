package ch.admin.bag.dp3t.updateboarding;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.util.AssetUtil;
import ch.admin.bag.dp3t.util.UlTagHandler;
import ch.admin.bag.dp3t.util.UrlUtil;

public class InteroperabilityUpdateBoardingFragment extends Fragment {

	public static InteroperabilityUpdateBoardingFragment newInstance() {
		return new InteroperabilityUpdateBoardingFragment();
	}

	public InteroperabilityUpdateBoardingFragment() {
		super(R.layout.fragment_update_boarding_interoperability);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TextView termsOfUseTextview = view.findViewById(R.id.terms_of_use_textview);
		TextView dataProtectionTextView = view.findViewById(R.id.data_protection_textview);
		termsOfUseTextview.setText(Html.fromHtml(AssetUtil.getTermsOfUse(getContext()), null, new UlTagHandler()));
		dataProtectionTextView.setText(Html.fromHtml(AssetUtil.getDataProtection(getContext()), null, new UlTagHandler()));

		ImageView termsOfUseChevron = view.findViewById(R.id.terms_of_use_chevron_imageview);
		ImageView dataProtectionChevron = view.findViewById(R.id.data_protection_chevron_imageview);

		View dataProtectionToOnlineVersionButton =
				view.findViewById(R.id.onboarding_disclaimer_data_protection_to_online_version_button);
		View termsOfUseToOnlineVersionButton = view.findViewById(R.id.onboarding_disclaimer_terms_of_use_to_online_version_button);

		View termsOfUseContainer = view.findViewById(R.id.onboarding_disclaimer_terms_of_use_container);
		View dataProtectionContainer = view.findViewById(R.id.onboarding_disclaimer_data_protection_container);

		view.findViewById(R.id.data_protection_header_container).setOnClickListener(v -> {
			if (dataProtectionContainer.getVisibility() == View.VISIBLE) {
				dataProtectionContainer.setVisibility(View.GONE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
			} else {
				dataProtectionContainer.setVisibility(View.VISIBLE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_light));
			}
			dataProtectionChevron.animate()
					.rotation(dataProtectionChevron.getRotation() + 180)
					.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
					.start();
		});

		view.findViewById(R.id.conditions_of_use_header_container).setOnClickListener(v -> {
			if (termsOfUseContainer.getVisibility() == View.VISIBLE) {
				termsOfUseContainer.setVisibility(View.GONE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
			} else {
				termsOfUseContainer.setVisibility(View.VISIBLE);
				v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_light));
			}
			termsOfUseChevron.animate()
					.rotation(termsOfUseChevron.getRotation() + 180)
					.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
					.start();
		});

		dataProtectionToOnlineVersionButton.setOnClickListener(v -> openOnlineVersion());
		termsOfUseToOnlineVersionButton.setOnClickListener(v -> openOnlineVersion());

		Button okButton = view.findViewById(R.id.updateboarding_ok_button);
		okButton.setOnClickListener(v -> ((UpdateBoardingActivity) getActivity()).finishUpdateBoarding());
	}

	private void openOnlineVersion() {
		UrlUtil.openUrl(getContext(), getString(R.string.onboarding_disclaimer_legal_button_url));
	}

}
