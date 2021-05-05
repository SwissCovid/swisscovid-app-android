package ch.admin.bag.dp3t.util

import androidx.fragment.app.Fragment
import ch.admin.bag.dp3t.R

fun Fragment.showFragment(fragment: Fragment){
	requireActivity().supportFragmentManager.beginTransaction()
		.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
		.replace(R.id.main_fragment_container, fragment)
		.addToBackStack(fragment::class.java.canonicalName)
		.commit()
}