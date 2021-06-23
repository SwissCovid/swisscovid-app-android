package ch.admin.bag.dp3t.extensions

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R

fun Fragment.showFragment(
	fragment: Fragment,
	@IdRes container: Int = R.id.main_fragment_container,
	modalAnimation: Boolean = false
) {
	requireActivity().supportFragmentManager.beginTransaction()
		.apply {
			if (modalAnimation) {
				this.setCustomAnimations(
					R.anim.modal_slide_enter, R.anim.modal_slide_exit, R.anim.modal_pop_enter, R.anim.modal_pop_exit
				)
			} else {
				this.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
			}
		}
		.addToBackStack(fragment::class.java.canonicalName)
		.replace(container, fragment)
		.commit()
}