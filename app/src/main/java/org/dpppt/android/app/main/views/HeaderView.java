/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package org.dpppt.android.app.main.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.model.AppState;

public class HeaderView extends FrameLayout {

	private static final long COLOR_ANIM_DURATION = 500;
	private static final long ICON_ANIM_DURATION = 500;
	private static final long ICON_ANIM_DELAY = 200;
	private static final float ANIM_OVERSHOOT_TENSION = 2;
	private static final long INITIAL_DELAY = 500;

	private ImageView backgroundImage;
	private ImageView icon;
	private ImageView iconBackground;

	private AppState currentState;
	private AnimatorSet iconAnimatorSet;
	private ValueAnimator colorAnimator;

	public HeaderView(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		setForegroundGravity(Gravity.CENTER);
		View headerContent = LayoutInflater.from(context).inflate(R.layout.view_header, this, true);
		backgroundImage = headerContent.findViewById(R.id.main_header_bg_image);
		backgroundImage.setForeground(new ColorDrawable(Color.TRANSPARENT));
		icon = headerContent.findViewById(R.id.main_header_icon);
		icon.setScaleX(0);
		icon.setScaleY(0);
		iconBackground = headerContent.findViewById(R.id.main_header_icon_bg);
		iconBackground.setScaleX(0);
		iconBackground.setScaleY(0);
	}

	public void setState(AppState state) {
		if (currentState == state) return;
		boolean initialUpdate = currentState == null;

		int backgroundColor = Color.TRANSPARENT;
		int iconRes = 0;
		int iconBgRes = 0;
		switch (state) {
			case TRACING_ON:
				iconRes = R.drawable.ic_begegnungen;
				iconBgRes = R.drawable.bg_header_icon_on;
				backgroundColor = getResources().getColor(R.color.header_bg_on, null);
				break;
			case TRACING_OFF:
				iconRes = R.drawable.ic_warning;
				iconBgRes = R.drawable.bg_header_icon_off;
				backgroundColor = getResources().getColor(R.color.header_bg_off, null);
				break;
			case ERROR_BLUETOOTH_OFF:
				iconRes = R.drawable.ic_bluetooth_off;
				iconBgRes = R.drawable.bg_header_icon_off;
				backgroundColor = getResources().getColor(R.color.header_bg_error, null);
				break;
			case ERROR_LOCATION_PERMISSION:
				iconRes = R.drawable.ic_location_off;
				iconBgRes = R.drawable.bg_header_icon_off;
				backgroundColor = getResources().getColor(R.color.header_bg_error, null);
				break;
			case ERROR_BATTERY_OPTIMIZATION:
				iconRes = R.drawable.ic_battery_alert;
				iconBgRes = R.drawable.bg_header_icon_off;
				backgroundColor = getResources().getColor(R.color.header_bg_error, null);
				break;
			case ERROR_SYNC_FAILED:
				iconRes = R.drawable.ic_sync_failed;
				iconBgRes = R.drawable.bg_header_icon_off;
				backgroundColor = getResources().getColor(R.color.header_bg_error, null);
				break;
			case EXPOSED:
				iconRes = R.drawable.ic_info;
				iconBgRes = R.drawable.bg_header_icon_off;
				backgroundColor = getResources().getColor(R.color.header_bg_exposed, null);
				break;
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

		if (iconAnimatorSet != null && iconAnimatorSet.isRunning()) iconAnimatorSet.cancel();
		if (initialUpdate) {
			Animator iconAnimator =
					createSizeAnimation(icon, icon.getScaleX(), 1, ICON_ANIM_DURATION, ICON_ANIM_DELAY + INITIAL_DELAY);
			Animator iconBgAnimator =
					createSizeAnimation(iconBackground, iconBackground.getScaleX(), 1, ICON_ANIM_DURATION, INITIAL_DELAY);
			icon.setImageResource(iconRes);
			iconBackground.setImageResource(iconBgRes);
			iconAnimatorSet = new AnimatorSet();
			iconAnimatorSet.playTogether(iconAnimator, iconBgAnimator);
			iconAnimatorSet.start();
		} else {
			iconAnimatorSet = createIconSwitchAnimation(icon, iconBackground, iconRes, iconBgRes, ICON_ANIM_DURATION);
			iconAnimatorSet.start();
		}

		currentState = state;
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

	private AnimatorSet createSizeBumpAnimation(View view, float to, long duration, Runnable onBumpPeak) {
		long halfDur = duration / 2;
		AnimatorSet animatorSet = new AnimatorSet();

		ValueAnimator bumpStart = createSizeAnimation(view, 1f, to, halfDur, 0);
		bumpStart.setInterpolator(new LinearInterpolator());
		bumpStart.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if (onBumpPeak != null) onBumpPeak.run();
			}
		});
		ValueAnimator bumpEnd = createSizeAnimation(view, to, 1f, halfDur, 0);

		animatorSet.playSequentially(bumpStart, bumpEnd);
		return animatorSet;
	}

	private AnimatorSet createIconSwitchAnimation(ImageView iconView, ImageView iconBg, @DrawableRes int iconRes,
			@DrawableRes int iconBgRes, long duration) {
		long halfDur = duration / 2;
		AnimatorSet animatorSet = new AnimatorSet();

		ValueAnimator disappearIcon = createSizeAnimation(iconView, 1f, 0f, halfDur, ICON_ANIM_DELAY);
		disappearIcon.setInterpolator(new AnticipateInterpolator(ANIM_OVERSHOOT_TENSION));
		disappearIcon.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				icon.setImageResource(iconRes);
			}
		});
		ValueAnimator appearIcon = createSizeAnimation(iconView, 0f, 1f, halfDur, 0);

		ValueAnimator disappearBg = createSizeAnimation(iconView, 1f, 0f, halfDur, 0);
		disappearBg.setInterpolator(new AnticipateInterpolator(ANIM_OVERSHOOT_TENSION));
		disappearBg.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				iconBg.setImageResource(iconBgRes);
			}
		});
		ValueAnimator appearBg = createSizeAnimation(iconView, 0f, 1f, halfDur, 0);

		animatorSet.playTogether(disappearIcon, disappearBg);
		animatorSet.play(appearIcon).after(disappearIcon);
		animatorSet.play(appearBg).after(disappearBg);
		return animatorSet;
	}

}
