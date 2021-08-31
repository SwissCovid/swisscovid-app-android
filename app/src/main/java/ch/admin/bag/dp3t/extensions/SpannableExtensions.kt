package ch.admin.bag.dp3t.extensions

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import java.util.regex.MatchResult
import java.util.regex.Pattern

/**
 * Search and replace in a spannable text using regular expressions, while keeping spans and allowing to add and remove spans.
 * @param pattern regular expression to match.
 * @param callback to replace the matched sequence with a new spanned value.
 */
fun Spannable.replace(
	pattern: Pattern,
	callback: (MatchResult, Spanned) -> Spanned
): Spannable {
	val matcher = pattern.matcher(toString())
		.useAnchoringBounds(false)
		.useTransparentBounds(true)
	val result = SpannableStringBuilder(this)
	while (matcher.find()) {
		val matchResult = matcher.toMatchResult()
		val matchedSequence = result.subSequence(matchResult.start(), matchResult.end()) as Spanned
		val replacement = callback(matchResult, matchedSequence)
		result.replace(matchResult.start(), matchResult.end(), replacement)
		matcher.reset(result).region(matchResult.start() + replacement.length, result.length)
	}
	return result
}
