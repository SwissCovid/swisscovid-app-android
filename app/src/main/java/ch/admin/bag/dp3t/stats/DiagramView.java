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
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.List;

import ch.admin.bag.dp3t.R;
import ch.admin.bag.dp3t.networking.models.HistoryDataPointModel;

@SuppressWarnings("FieldCanBeLocal")
public class DiagramView extends View {

	public static final int OFFSET_BOTTOM_X_AXIS = 33;
	private static final int STROKE_WIDTH_X_AXIS = 1;
	private static final int HEIGHT_X_AXIS_DATE_MARKER = 10;
	private static final int PADDING_X_AXIS_DATE_MARKER_LABEL = 5;

	private static final int STROKE_WIDTH_Y_AXIS = 1;
	private static final int WIDTH_Y_AXIS_DASH_ON = 2;
	private static final int WIDTH_Y_AXIS_DASH_OFF = 2;

	private static final int STROKE_WIDTH_AVG_LINE = 2;
	private static final int WIDTH_BAR = 10;
	private static final int PADDING_BAR = 1;

	// All sizes are given in DP and need to be scaled/multiplied by this value
	private float dp;

	private List<HistoryDataPointModel> history;
	private int maxYValue;

	private int canvasXTranslation;

	private Paint newInfectionsPaint;
	private Paint newInfectionsAvgPaint;
	private Paint enteredCovidcodesPaint;
	private Paint xAxisPaint;
	private Paint yAxisPaint;
	private Paint labelPaint;

	Path newInfectionsAvgPath = new Path();

	Calendar calendar = Calendar.getInstance();
	Rect textRect = new Rect();

	Path yAxisPath = new Path();

	public DiagramView(Context context) {
		this(context, null);
	}

	public DiagramView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public DiagramView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		dp = context.getResources().getDisplayMetrics().density;

		int newInfectionsPaintColor = getResources().getColor(R.color.stats_diagram_new_infections, null);
		newInfectionsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		newInfectionsPaint.setStyle(Paint.Style.FILL);
		newInfectionsPaint.setColor(newInfectionsPaintColor);

		int newInfectionsAvgPaintColor = getResources().getColor(R.color.stats_diagram_new_infections_avg, null);
		newInfectionsAvgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		newInfectionsAvgPaint.setStyle(Paint.Style.STROKE);
		newInfectionsAvgPaint.setColor(newInfectionsAvgPaintColor);
		newInfectionsAvgPaint.setStrokeWidth(STROKE_WIDTH_AVG_LINE * dp);

		int enteredCovidcodesPaintColor = getResources().getColor(R.color.stats_diagram_entered_covidcodes, null);
		enteredCovidcodesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		enteredCovidcodesPaint.setStyle(Paint.Style.FILL);
		enteredCovidcodesPaint.setColor(enteredCovidcodesPaintColor);

		int xAxisPaintColor = getResources().getColor(R.color.stats_diagram_x_axis, null);
		xAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		xAxisPaint.setStyle(Paint.Style.STROKE);
		xAxisPaint.setColor(xAxisPaintColor);
		xAxisPaint.setStrokeWidth(STROKE_WIDTH_X_AXIS * dp);

		int yAxisPaintColor = getResources().getColor(R.color.stats_diagram_y_axis, null);
		DashPathEffect pathEffect = new DashPathEffect(new float[] { WIDTH_Y_AXIS_DASH_ON * dp, WIDTH_Y_AXIS_DASH_OFF * dp }, 0);
		yAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		yAxisPaint.setStyle(Paint.Style.STROKE);
		yAxisPaint.setColor(yAxisPaintColor);
		yAxisPaint.setStrokeWidth(STROKE_WIDTH_Y_AXIS * dp);
		yAxisPaint.setPathEffect(pathEffect);

		int labelPaintColor = getResources().getColor(R.color.stats_diagram_labels, null);
		float labelTextSize = context.getResources().getDimension(R.dimen.text_size_small);
		labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		labelPaint.setStyle(Paint.Style.FILL);
		labelPaint.setColor(labelPaintColor);
		labelPaint.setTextSize(labelTextSize);
		labelPaint.setTextAlign(Paint.Align.LEFT);
	}

	public void setHistory(List<HistoryDataPointModel> history) {
		this.history = history;
		this.maxYValue = findMaxYValue(history);
		invalidate();
	}

	/**
	 * Returns the width in DP that the DiagramView would need on an infinite screen.
	 */
	public int getTotalTheoreticWidth() {
		return Math.round(history.size() * WIDTH_BAR * dp + (history.size() - 1) * PADDING_BAR * dp);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Throughout the drawing functions, we often use float (rather than int)
		// to avoid having to explicitly cast to float in all the calculations

		if (history == null || history.isEmpty()) {
			return;
		}

		canvas.translate(-canvasXTranslation, 0);

		drawData(canvas);
		drawXAxis(canvas);
		drawYAxis(canvas);
	}

	public void setScrollX(int x) {
		canvasXTranslation = x;
		invalidate();
	}

	private void drawData(Canvas canvas) {
		newInfectionsAvgPath.reset();

		boolean prevValueMissing = true;
		for (int i = 0; i < history.size(); i++) {
			float ni = history.get(i).getNewInfections();
			Integer niavg = history.get(i).getNewInfectionsSevenDayAverage();
			float cc = history.get(i).getCovidcodesEntered();

			// Draw bars
			float left = i * (WIDTH_BAR + PADDING_BAR) * dp;
			float right = left + WIDTH_BAR * dp;

			float bottom = getHeight() - OFFSET_BOTTOM_X_AXIS * dp;
			float topCovidcodes = bottom - bottom * (cc / maxYValue);

			float bottomNewInfections = topCovidcodes;
			if (cc > 0) {
				bottomNewInfections -= PADDING_BAR * dp;
			}
			float topNewInfections = bottomNewInfections - bottom * (ni / maxYValue);

			canvas.drawRect(left, topCovidcodes, right, bottom, enteredCovidcodesPaint);
			canvas.drawRect(left, topNewInfections, right, bottomNewInfections, newInfectionsPaint);

			// Prepare drawing average-new-infections line
			float avgX = left + WIDTH_BAR * dp / 2;
			float avgY = 0;
			if (niavg != null) {
				avgY = bottom - bottom * (niavg * 1.0f / maxYValue);
				if (prevValueMissing) {
					newInfectionsAvgPath.moveTo(avgX, avgY);
				} else {
					newInfectionsAvgPath.lineTo(avgX, avgY);
				}
				prevValueMissing = false;
			} else {
				prevValueMissing = true;
			}
		}

		canvas.drawPath(newInfectionsAvgPath, newInfectionsAvgPaint);
	}

	private void drawXAxis(Canvas canvas) {
		float xAxisHeight = getHeight() - OFFSET_BOTTOM_X_AXIS * dp;
		canvas.drawLine(0, xAxisHeight, getTotalTheoreticWidth(), xAxisHeight, xAxisPaint);

		for (int i = 0; i < history.size(); i++) {
			calendar.setTime(history.get(i).getDateParsed());

			if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
				float centerX = i * (WIDTH_BAR + PADDING_BAR) * dp + (WIDTH_BAR * dp) / 2F;

				// Draw marker (a vertical line)
				float topMarker = xAxisHeight;
				float bottomMarker = xAxisHeight + HEIGHT_X_AXIS_DATE_MARKER * dp;
				canvas.drawLine(centerX, topMarker, centerX, bottomMarker, xAxisPaint);

				// Draw label
				// Note that the origin of the text is the BOTTOM LEFT coordinate (not the top left)!!!
				String xLabel = history.get(i).getDateFormatted();
				labelPaint.getTextBounds(xLabel, 0, xLabel.length(), textRect);
				float textX = centerX - textRect.width() / 2F;
				float textY = bottomMarker + PADDING_X_AXIS_DATE_MARKER_LABEL * dp + textRect.height();
				canvas.drawText(xLabel, textX, textY, labelPaint);
			}
		}
	}

	private void drawYAxis(Canvas canvas) {
		float bottom = getHeight() - OFFSET_BOTTOM_X_AXIS * dp;

		for (int yLabel : DiagramYAxisView.getYAxisLabelValues(maxYValue)) {
			// Don't draw a horizontal line for this axis label
			if (yLabel != 0) {
				float yAxisHeight = bottom - bottom * (yLabel / (float) maxYValue);
				yAxisPath.reset();
				yAxisPath.moveTo(0, yAxisHeight);
				yAxisPath.lineTo(getTotalTheoreticWidth(), yAxisHeight);
				// Use a path for the dashed PathEffect
				canvas.drawPath(yAxisPath, yAxisPaint);
			}
		}
	}

	public static int findMaxYValue(List<HistoryDataPointModel> hist) {
		int max = Integer.MIN_VALUE;
		for (HistoryDataPointModel point : hist) {
			if (point.getNewInfections() > max) {
				max = point.getNewInfections();
			}
			if (point.getNewInfectionsSevenDayAverage() != null && point.getNewInfectionsSevenDayAverage() > max) {
				max = point.getNewInfectionsSevenDayAverage();
			}
			if (point.getCovidcodesEntered() > max) {
				max = point.getCovidcodesEntered();
			}
		}
		return max;
	}

}
