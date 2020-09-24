package ch.admin.bag.dp3t.util;

import java.util.Stack;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;

public class UlTagHandler implements Html.TagHandler {
	private static final int indent = 30;
	public static String LI = "myli";


	@Override
	public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
	if (tag.equals(LI)) {
			if (opening) {
				if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
					output.append("\n");
				}
				start(output, new Ul());
			} else {
				if (output.charAt(output.length() - 1) != '\n') {
					output.append("\n");
				}
				BulletSpan newBullet = new BulletSpan(indent);
				end(output, Ul.class, newBullet);
			}
		}
	}

	private static void start(Editable text, Object mark) {
		int len = text.length();
		text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
	}

	private static void end(Editable text, Class<?> kind, Object... replaces) {
		int len = text.length();
		Object obj = getLast(text, kind);
		int where = text.getSpanStart(obj);
		text.removeSpan(obj);
		if (where != len) {
			for (Object replace : replaces) {
				text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	private static Object getLast(Spanned text, Class<?> kind) {
		Object[] objs = text.getSpans(0, text.length(), kind);
		if (objs.length == 0) {
			return null;
		}
		return objs[objs.length - 1];
	}

	private static class Ul { }

}
