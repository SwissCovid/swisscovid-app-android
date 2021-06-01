/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.home.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Random;

import org.dpppt.android.sdk.TracingStatus;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.home.model.NotificationState;
import ch.admin.bag.dp3t.home.model.TracingState;
import ch.admin.bag.dp3t.home.model.TracingStatusInterface;
import ch.admin.bag.dp3t.util.TracingErrorStateHelper;

public class HeaderView extends ConstraintLayout {

	private static final float ANIM_OVERSHOOT_TENSION = 2;
	private static final long COLOR_ANIM_DURATION = 500;
	static final long ICON_ANIM_DURATION = 500;
	static final long ICON_ANIM_DELAY = 200;
	static final long INITIAL_DELAY = 500;

	private static final int[] BACKGROUND_IMAGES =
			new int[] { R.drawable.header_basel,
					R.drawable.header_bern,
					R.drawable.header_chur,
					R.drawable.header_geneva,
					R.drawable.header_lausanne,
					R.drawable.header_locarno,
					R.drawable.header_lugano,
					R.drawable.header_luzern,
					R.drawable.header_stgallen,
					R.drawable.header_zurich };
	private static Integer backgroundImageIndex = null;

	private ImageView backgroundImage;
	private ImageView icon;
	private ImageView iconBackground;
	private CircleAnimationView circleView;

	private TracingState currentTracingState;
	private NotificationState currentNotificationState;
	private TracingStatus.ErrorState currentErrorState;
	private AnimatorSet iconAnimatorSet;
	private ValueAnimator colorAnimator;

	public HeaderView(Context context) {
		super(context);
		init(context, null, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr) {
		setForegroundGravity(Gravity.CENTER);
		View headerContent = LayoutInflater.from(context).inflate(R.layout.view_header, this, true);
		icon = headerContent.findViewById(R.id.main_header_icon);
		icon.setScaleX(0);
		icon.setScaleY(0);
		iconBackground = headerContent.findViewById(R.id.main_header_icon_bg);
		iconBackground.setScaleX(0);
		iconBackground.setScaleY(0);

		if (backgroundImageIndex == null) {
			backgroundImageIndex = new Random(System.currentTimeMillis()).nextInt(BACKGROUND_IMAGES.length);
		}
		backgroundImage = headerContent.findViewById(R.id.main_header_bg_image);
		backgroundImage.setForeground(new ColorDrawable(getResources().getColor(R.color.header_bg_off, null)));
		backgroundImage.setImageResource(BACKGROUND_IMAGES[backgroundImageIndex]);

		circleView = headerContent.findViewById(R.id.main_header_anim_view);
	}

	public void stopAnimation() {
		circleView.stopAnimation();
	}

	public void setState(TracingStatusInterface state) {
		boolean initialUpdate = currentTracingState == null;

		if (state.getTracingState() == currentTracingState && state.getNotificationState() == currentNotificationState &&
				state.getTracingErrorState() == currentErrorState) {
			return;
		}

		currentErrorState = state.getTracingErrorState();
		currentTracingState = state.getTracingState();
		currentNotificationState = state.getNotificationState();

		int backgroundColor;
		int iconRes = 0;
		Integer iconTintColor = null;
		int iconBgRes = 0;
		TracingStatus.ErrorState error = state.getTracingErrorState();
		boolean hasTracingError = error != null && TracingErrorStateHelper.isTracingErrorState(error);
		if (state.getNotificationState() == NotificationState.NO_REPORTS ||
				state.getNotificationState() == NotificationState.EXPOSED) {
			if (currentTracingState == TracingState.ACTIVE && hasTracingError) {
				iconBgRes = R.drawable.bg_header_icon_off;
				iconTintColor = R.color.white;
				backgroundColor = getResources().getColor(R.color.header_bg_error, null);
				switch (error) {
					case BLE_DISABLED:
						iconRes = R.drawable.ic_bluetooth_off;
						break;
					case LOCATION_SERVICE_DISABLED:
						iconRes = R.drawable.ic_header_gps_off;
						break;
					default:
						iconRes = R.drawable.ic_warning;
				}
			} else {
				iconRes = R.drawable.ic_begegnungen;
				iconTintColor = R.color.white;
				iconBgRes = R.drawable.bg_header_icon_on;
				backgroundColor = getResources().getColor(R.color.header_bg_on, null);
			}
		} else if (state.getNotificationState() == NotificationState.POSITIVE_TESTED) {
			backgroundColor = getResources().getColor(R.color.header_bg_exposed, null);
		} else {
			throw new IllegalStateException(
					"Unhandled tracing status in header: \n" + state.getNotificationState() + "\n" + state.getTracingState() +
							"\n" + state.getTracingErrorState());
		}
		iconBackground.setImageResource(R.drawable.ic_header_background);

		if (colorAnimator != null && colorAnimator.isRunning()) colorAnimator.cancel();
		ColorDrawable colorDrawable = (ColorDrawable) backgroundImage.getForeground();
		int startColor = colorDrawable.getColor();
		int endColor = backgroundColor;
		colorAnimator = ValueAnimator.ofArgb(startColor, endColor);
		colorAnimator.setDuration(COLOR_ANIM_DURATION);
		colorAnimator.addUpdateListener(animation -> colorDrawable.setColor((int) animation.getAnimatedValue()));
		colorAnimator.start();

		if (initialUpdate) {
			if (iconAnimatorSet != null && iconAnimatorSet.isRunning()) iconAnimatorSet.cancel();
			Animator iconAnimator =
					createSizeAnimation(icon, icon.getScaleX(), 1, ICON_ANIM_DURATION, ICON_ANIM_DELAY + INITIAL_DELAY);
			Animator iconBgAnimator =
					createSizeAnimation(iconBackground, iconBackground.getScaleX(), 1, ICON_ANIM_DURATION, INITIAL_DELAY);
			icon.setImageResource(iconRes);
			if (iconTintColor != null) {
				icon.setImageTintList(ContextCompat.getColorStateList(getContext(), iconTintColor));
			} else {
				icon.setImageTintList(null);
			}
			iconBackground.setImageResource(iconBgRes);
			iconAnimatorSet = new AnimatorSet();
			iconAnimatorSet.playTogether(iconAnimator, iconBgAnimator);
			iconAnimatorSet.start();
		} else {
			icon.setImageResource(iconRes);
			if (iconTintColor != null) {
				icon.setImageTintList(ContextCompat.getColorStateList(getContext(), iconTintColor));
			} else {
				icon.setImageTintList(null);
			}
			iconBackground.setImageResource(iconBgRes);
		}

		circleView.setState(
				state.getNotificationState() != NotificationState.POSITIVE_TESTED
						&& state.getTracingState() == TracingState.ACTIVE
						&& !hasTracingError,
				initialUpdate);
		icon.post(() -> {
			circleView.setCenter(Math.round(icon.getX() + icon.getWidth() / 2f), Math.round(icon.getY() + icon.getHeight() / 2f));
		});
	}

	private ValueAnimator createSizeAnimation(View view, float from, float to, long duration, long delay) {
		ValueAnimator animator = ValueAnimator.ofFloat(from, to);
		animator.setInterpolator(new OvershootInterpolator(ANIM_OVERSHOOT_TENSION));
		animator.setDuration(duration);
		animator.setStartDelay(delay);
		animator.addUpdateListener(animation -> {
			float scale = (float) animation.getAnimatedValue();
			view.setScaleX(scale);
			view.setScaleY(scale);
		});
		return animator;
	}

	private ValueAnimator createAlphaAnimation(View view, float from, float to, long duration, long delay) {
		ValueAnimator animator = ValueAnimator.ofFloat(from, to);
		animator.setInterpolator(new DecelerateInterpolator());
		animator.setDuration(duration);
		animator.setStartDelay(delay);
		animator.addUpdateListener(animation -> {
			float alpha = (float) animation.getAnimatedValue();
			view.setAlpha(alpha);
			view.setAlpha(alpha);
		});
		return animator;
	}

}
