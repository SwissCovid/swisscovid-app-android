@file:JvmName("AccessibilityUtil")

package ch.admin.bag.dp3t.util

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button

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
