/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package ch.admin.bag.dp3t.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import ch.admin.bag.dp3t.R;

@SuppressWarnings("FieldCanBeLocal")
public class DiagramYAxisView extends View {

	private static final int PADDING_Y_AXIS_LEFT = 5;
	private static final int PADDING_Y_AXIS_RIGHT = 10;

	// All sizes are given in DP and need to be scaled/multiplied by this value
	private float dp;

	private int maxYValue;
	private float paddingTopDp;

	private Paint labelPaint;
	Rect textRect = new Rect();

	public DiagramYAxisView(Context context) {
		this(context, null);
	}

	public DiagramYAxisView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DiagramYAxisView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public DiagramYAxisView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		dp = context.getResources().getDisplayMetrics().density;

		paddingTopDp = context.getResources().getDimension(R.dimen.stats_diagram_padding_top);

		int labelPaintColor = context.getResources().getColor(R.color.stats_diagram_labels, null);
		float labelTextSize = context.getResources().getDimension(R.dimen.text_size_small);
		Typeface labelTypeface = ResourcesCompat.getFont(context, R.font.inter_regular);
		labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setStyle(Paint.Style.FILL);
		labelPaint.setColor(labelPaintColor);
		labelPaint.setTextSize(labelTextSize);
		labelPaint.setTypeface(labelTypeface);
		labelPaint.setTextAlign(Paint.Align.LEFT);
	}

	public void setMaxYValue(int maxYValue) {
		this.maxYValue = maxYValue;
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		String longestLabel = Integer.toString(maxYValue);
		labelPaint.getTextBounds(longestLabel, 0, longestLabel.length(), textRect);

		int width = (int) (textRect.width() + (PADDING_Y_AXIS_LEFT + PADDING_Y_AXIS_RIGHT) * dp);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawYAxisLabels(canvas);
	}

	private void drawYAxisLabels(Canvas canvas) {
		float xAxisYCoord = getHeight() - DiagramView.OFFSET_BOTTOM_X_AXIS * dp;
		float diagramHeight = xAxisYCoord - paddingTopDp;

		for (int yLabel : getYAxisLabelValues(maxYValue)) {
			String label = Integer.toString(yLabel);

			// Note that the origin of the text is the BOTTOM LEFT coordinate (not the top left)!!!
			labelPaint.getTextBounds(label, 0, label.length(), textRect);
			float bottom = xAxisYCoord								// baseline
					+ textRect.height() / 2F						// center text vertically
					- (yLabel / (float) maxYValue) * diagramHeight;	// move back up according to the y-label value

			canvas.drawText(label, PADDING_Y_AXIS_LEFT * dp, bottom, labelPaint);
		}
	}

	/**
	 * For the counts/numbers/values of the data points in [history],
	 * get the appropriate values that the y-axis should display.
	 * <p>
	 * This is always 0 plus some evenly spaced, nice round values.
	 */
	public static int[] getYAxisLabelValues(int maxYValue) {
		assert (maxYValue >= 0);
		if (maxYValue == 0) {
			return new int[] { 0 };
		}

		double tempStepSize = maxYValue / 4.0;
		double mag = Math.floor(Math.log10(tempStepSize));
		double magPow = Math.pow(10, mag);
		int magMsd = (int) (tempStepSize / magPow + 0.5);
		int stepSize = (int) (magMsd * magPow);

		int numLabels = (int) Math.ceil((double) maxYValue / stepSize);
		assert (numLabels > 0);

		int[] values = new int[numLabels];
		values[0] = 0;
		for (int i = 1; i < numLabels; i++) {
			values[i] = values[i - 1] + stepSize;
		}
		return values;
	}

}
