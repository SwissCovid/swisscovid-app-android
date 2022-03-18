package ch.admin.bag.dp3t.hibernate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.TabbarHostFragment
import ch.admin.bag.dp3t.databinding.FragmentHibernatingInfoBinding
import ch.admin.bag.dp3t.html.HtmlFragment
import ch.admin.bag.dp3t.networking.models.InfoBoxModel
import ch.admin.bag.dp3t.util.AssetUtil
import ch.admin.bag.dp3t.util.UrlUtil

class HibernatingInfoFragment : Fragment() {

	companion object {
		@JvmStatic
		fun newInstance() = HibernatingInfoFragment()
	}

	private val viewModel: HibernatingViewModel by viewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return FragmentHibernatingInfoBinding.inflate(inflater).apply {

			toolbar.setOnMenuItemClickListener {
				showImpressum()
				true
			}

			viewModel.isHibernatingModeEnabled.observe(viewLifecycleOwner) { isHibernatingModeEnabled ->
				if (!isHibernatingModeEnabled) {
					showHomeFragment()
				}
			}

			viewModel.hibernatingInfoBox.observe(viewLifecycleOwner) { infoBoxModelCollection ->
				val infobox = infoBoxModelCollection?.getInfoBox(getString(R.string.language_key))
				infobox?.let {
					title.text = it.title
					text.text = it.msg
					linkGroup.isVisible = it.urlTitle != null
					linkText.text = it.urlTitle
					linkGroup.setOnClickListener { v -> UrlUtil.openUrl(requireContext(), it.url) }
				}
			}

		}.root
	}

	private fun showHomeFragment() {
		requireActivity().supportFragmentManager.beginTransaction()
			.replace(R.id.main_fragment_container, TabbarHostFragment.newInstance())
			.commit()
	}

	private fun showImpressum() {
		val htmlFragment = HtmlFragment.newInstance(
			R.string.menu_impressum, AssetUtil.getImpressumBaseUrl(context),
			AssetUtil.getImpressumHtml(context)
		)
		requireActivity().supportFragmentManager.beginTransaction()
			.setCustomAnimations(
				R.anim.slide_enter,
				R.anim.slide_exit,
				R.anim.slide_pop_enter,
				R.anim.slide_pop_exit
			)
			.replace(R.id.main_fragment_container, htmlFragment)
			.addToBackStack(HtmlFragment::class.java.canonicalName)
			.commit()
	}

}
