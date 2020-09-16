package ch.admin.bag.dp3t.stats;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import ch.admin.bag.dp3t.networking.models.StatsResponseModel;

public class DiagramView extends View {

	private StatsResponseModel stats;

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
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

}
