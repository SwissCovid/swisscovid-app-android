package ch.admin.bag.dp3t.onboarding

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.FragmentUpdateBoardingBinding
import ch.admin.bag.dp3t.util.AssetUtil
import ch.admin.bag.dp3t.util.UlTagHandler
import ch.admin.bag.dp3t.util.UrlUtil

class UpdateBoardingFragment : Fragment() {


	companion object {
		fun newInstance() = UpdateBoardingFragment()
	}


	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		return FragmentUpdateBoardingBinding.inflate(inflater).apply {
			termsOfUseTextview.text = Html.fromHtml(AssetUtil.getTermsOfUse(context), null, UlTagHandler())
			dataProtectionTextview.text = Html.fromHtml(AssetUtil.getDataProtection(context), null, UlTagHandler())

			dataProtectionHeaderContainer.setOnClickListener { v ->

				dataProtectionContainer.isVisible = !dataProtectionContainer.isVisible
				v.setBackgroundColor(
					ContextCompat.getColor(v.context, if (dataProtectionContainer.isVisible) R.color.grey_light else R.color.white)
				)

				dataProtectionChevronImageview.animate()
					.rotation(dataProtectionChevronImageview.rotation + 180)
					.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
					.start()
			}


			conditionsOfUseHeaderContainer.setOnClickListener { v: View ->
				termsOfUseContainer.isVisible = !termsOfUseContainer.isVisible
				v.setBackgroundColor(
					ContextCompat.getColor(v.context, if (termsOfUseContainer.isVisible) R.color.grey_light else R.color.white)
				)

				termsOfUseChevronImageview.animate()
					.rotation(termsOfUseChevronImageview.rotation + 180)
					.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
					.start()
			}
			dataProtectionToOnlineVersionButton.setOnClickListener { openOnlineVersion() }
			termsOfUseToOnlineVersionButton.setOnClickListener { openOnlineVersion() }
			updateboardingOkButton.setOnClickListener { (activity as OnboardingActivity).continueToNextPage() }

		}.root
	}

	private fun openOnlineVersion() {
		UrlUtil.openUrl(context, getString(R.string.onboarding_disclaimer_legal_button_url))
	}

}