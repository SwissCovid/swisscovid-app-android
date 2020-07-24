package ch.admin.bag.dp3t.util;

import java.util.Stack;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.util.Log;


/**
 * Implements support for ordered and unordered lists in to Android TextView.
 * <p>
 * Some code taken from inner class android.text.Html.HtmlToSpannedConverter. If you find this code useful,
 * please vote my answer at <a href="http://stackoverflow.com/a/17365740/262462">StackOverflow</a> up.
 */
public class MyTagHandler implements Html.TagHandler {
	Stack<String> lists = new Stack<String>();
	Stack<Integer> olNextIndex = new Stack<Integer>();
	private static final int indent = 0;
	private static final int listItemIndent = indent * 2;
	private static final BulletSpan bullet = new BulletSpan(indent);

	public static String UL = "myUL";
	public static String OL = "myOL";
	public static String LI = "myLi";


	@Override
	public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
		if (tag.equals(UL)) {
			if (opening) {
				lists.push(tag);
			} else {
				lists.pop();
			}
		} else if (tag.equals(OL)) {
			if (opening) {
				lists.push(tag);
				olNextIndex.push(1);
			} else {
				lists.pop();
				olNextIndex.pop();
			}
		} else if (tag.equals(LI)) {
			if (opening) {
				if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
					output.append("\n");
				}
				String parentList = lists.peek();
				if (parentList.equals(OL)) {
					start(output, new Ol());
					output.append(olNextIndex.peek().toString() + ". ");
					olNextIndex.push(olNextIndex.pop() + 1);
				} else if (parentList.equalsIgnoreCase(UL)) {
					start(output, new Ul());
				}
			} else {
				if (lists.peek().equalsIgnoreCase(UL)) {
					if (output.charAt(output.length() - 1) != '\n') {
						output.append("\n");
					}
					BulletSpan newBullet = new BulletSpan(indent);
					int numberMargin = listItemIndent * (lists.size() - 1);
					end(output, Ul.class, new LeadingMarginSpan.Standard(numberMargin, numberMargin), newBullet);
				} else if (lists.peek().equalsIgnoreCase(OL)) {
					if (output.charAt(output.length() - 1) != '\n') {
						output.append("\n");
					}
					int numberMargin = listItemIndent * (lists.size() - 1);
					if (lists.size() == 1) end(output, Ol.class, new LeadingMarginSpan.Standard(numberMargin + 30, numberMargin));
					else end(output, Ol.class, new LeadingMarginSpan.Standard(numberMargin, numberMargin));
				}
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
		return;
	}

	private static Object getLast(Spanned text, Class<?> kind) {
		/*
		 * This knows that the last returned object from getSpans()
		 * will be the most recently added.
		 */
		Object[] objs = text.getSpans(0, text.length(), kind);
		if (objs.length == 0) {
			return null;
		}
		return objs[objs.length - 1];
	}

	private static class Ul { }


	private static class Ol { }

}
