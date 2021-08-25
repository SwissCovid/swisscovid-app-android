@file:JvmName("AccessibilityUtil")

package ch.admin.bag.dp3t.util

import android.view.View
import android.view.accessibility.AccessibilityNodeInfo

fun View.setExpansionToggleStateAccessibilityDelegate(expandableView: View) {
	accessibilityDelegate = object : View.AccessibilityDelegate() {
		override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
			info.addAction(
				if (expandableView.visibility == View.VISIBLE)
					AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE
				else
					AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND
			)
			super.onInitializeAccessibilityNodeInfo(host, info);
		}
	}
}
