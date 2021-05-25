package ch.admin.bag.dp3t.extensions

import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R

fun Fragment.showFragment(fragment: Fragment, addToBackStack: Boolean = true) {
	requireActivity().supportFragmentManager.beginTransaction()
		.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
		.replace(R.id.main_fragment_container, fragment)
		.apply { if (addToBackStack) addToBackStack(fragment::class.java.canonicalName) }
		.commit()
}