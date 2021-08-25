@file:JvmName("AccessibilityUtil")

package ch.admin.bag.dp3t.util

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import androidx.core.view.doOnLayout

fun Context.isAccessibilityActive(): Boolean {
	val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
	return am != null && am.isEnabled && am.isTouchExplorationEnabled
}

fun View.requestAccessibilityFocus() {
	doOnLayout {
		performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
		sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
	}
}

fun View.setExpansionToggleStateAccessibilityDelegate(expandableView: View) {
	accessibilityDelegate = object : View.AccessibilityDelegate() {
		override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
			info.addAction(
				if (expandableView.visibility == View.VISIBLE)
					AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE
				else
					AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND
			)
			super.onInitializeAccessibilityNodeInfo(host, info)
		}
	}
}

fun View.setButtonAccessibilityDelegate() {
	accessibilityDelegate = object : View.AccessibilityDelegate() {
		override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
			super.onInitializeAccessibilityNodeInfo(host, info)
			info.className = Button::class.java.name
		}
	}
}
