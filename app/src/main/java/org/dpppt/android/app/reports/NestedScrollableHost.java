package org.dpppt.android.app.reports;

/**
 * Copyright 2019 The Android Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import org.dpppt.android.app.MainActivity;

/**
 * Layout to wrap a scrollable component inside a ViewPager2. Provided as a solution to the problem
 * where pages of ViewPager2 have nested scrollable elements that scroll in the same direction as
 * ViewPager2. The scrollable element needs to be the immediate and only child of this host layout.
 * <p>
 * This solution has limitations when using multiple levels of nested scrollable elements
 * (e.g. a horizontal RecyclerView in a vertical RecyclerView in a horizontal ViewPager2).
 */
public class NestedScrollableHost extends FrameLayout {

	public NestedScrollableHost(@NonNull Context context) {
		super(context);
	}

	public NestedScrollableHost(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	private int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	private float initialX = 0f;
	private float initialY = 0f;

	private ViewPager2 getParentViewPager() {
		Activity activity = getActivity();
		if(activity instanceof MainActivity){
			return ((MainActivity)activity).getMainViewPager();
		}

		return null;
	}

	private Activity getActivity() {
		Context context = getContext();
		while (context instanceof ContextWrapper) {
			if (context instanceof Activity) {
				return (Activity) context;
			}
			context = ((ContextWrapper) context).getBaseContext();
		}
		return null;
	}

	private boolean canChildScroll(int orientation, float delta) {

		View child = getChildCount() > 0 ? getChildAt(0) : null;
		if (child == null) return false;

		int direction = (int) Math.signum(-delta);
		if (orientation == 0) {
			return child.canScrollHorizontally(direction);
		} else if (orientation == 1) {
			return child.canScrollVertically(direction);
		} else {
			throw new IllegalArgumentException();
		}
	}



	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		handleInterceptTouchEvent(e);
		return super.onInterceptTouchEvent(e);
	}

	private void handleInterceptTouchEvent(MotionEvent e) {
		ViewPager2 parentViewPager = getParentViewPager();
		if (parentViewPager == null) return;

		int orientation = parentViewPager.getOrientation();

		// Early return if child can't scroll in same direction as parent
		if (!canChildScroll(orientation, -1f) && !canChildScroll(orientation, 1f)) {
			return;
		}

		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			initialX = e.getX();
			initialY = e.getY();
			getParent().requestDisallowInterceptTouchEvent(true);
		} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			float dx = e.getX() - initialX;
			float dy = e.getY() - initialY;
			boolean isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL;

			// assuming ViewPager2 touch-slop is 2x touch-slop of child
			float scaledDx = Math.abs(dx) * (isVpHorizontal ? .5f : 1f);
			float scaledDy = Math.abs(dy) * (isVpHorizontal ? 1f : .5f);

			if (scaledDx > touchSlop || scaledDy > touchSlop) {
				if (isVpHorizontal == (scaledDy > scaledDx)) {
					// Gesture is perpendicular, allow all parents to intercept
					getParent().requestDisallowInterceptTouchEvent(false);
				} else {
					// Gesture is parallel, query child if movement in that direction is possible
					if (canChildScroll(orientation, isVpHorizontal ? dx : dy)) {
						// Child can scroll, disallow all parents to intercept
						getParent().requestDisallowInterceptTouchEvent(true);
					} else {
						// Child cannot scroll, allow all parents to intercept
						getParent().requestDisallowInterceptTouchEvent(false);
					}
				}
			}
		}
	}

}