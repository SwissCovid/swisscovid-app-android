package ch.admin.bag.dp3t.util;

import android.graphics.Typeface;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import androidx.annotation.NonNull;

public class CustomStyleUrlSpan extends URLSpan {

	private int textColor;
	private Typeface typeface;

	public CustomStyleUrlSpan(String url) {
		super(url);
	}

	public CustomStyleUrlSpan(@NonNull Parcel src) {
		super(src);
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public void setTypeface(Typeface typeface) {
		this.typeface = typeface;
	}

	@Override
	public void updateDrawState(@NonNull TextPaint ds) {
		super.updateDrawState(ds);
		ds.setUnderlineText(false);
		ds.setColor(textColor);
		ds.setTypeface(typeface);
	}

	@Override
	public void onClick(@NonNull View widget) {
		super.onClick(widget);
	}

}
