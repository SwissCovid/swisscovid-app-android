package ch.admin.bag.dp3t.checkin.checkout

import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.bold
import androidx.fragment.app.DialogFragment
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.extensions.replace
import ch.admin.bag.dp3t.util.StringUtil.getHourMinuteTimeString
import java.util.regex.Pattern

class CheckOutConflictResolvedDialogFragment : DialogFragment() {

	companion object {
		val TAG = CheckOutConflictResolvedDialogFragment::class.java.canonicalName

		private const val ARG_LOCATION = "LOCATION"
		private const val ARG_CHECKIN_TIME = "CHECKIN_TIME"
		private const val ARG_CHECKOUT_TIME = "CHECKOUT_TIME"

		fun newInstance(location: String, checkinTime: Long, checkoutTime: Long): CheckOutConflictResolvedDialogFragment {
			val fragment = CheckOutConflictResolvedDialogFragment()
			fragment.arguments = Bundle().apply {
				putString(ARG_LOCATION, location)
				putLong(ARG_CHECKIN_TIME, checkinTime)
				putLong(ARG_CHECKOUT_TIME, checkoutTime)
			}
			return fragment
		}
	}

	private var onCheckoutListener: (() -> Unit)? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.dialog_fragment_checkout_conflict_resolved, container)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		val closeButton = view.findViewById<View>(R.id.checkout_resolved_close_button)
		closeButton.setOnClickListener { dismiss() }

		val location = requireArguments().getString(ARG_LOCATION)
		val checkinTime = requireArguments().getLong(ARG_CHECKIN_TIME)
		val checkoutTime = requireArguments().getLong(ARG_CHECKOUT_TIME)

		val checkoutResolvedText = view.findViewById<TextView>(R.id.checkout_resolved_text)
		checkoutResolvedText.text =
			SpannableString(getText(R.string.checkin_overlap_popup_success_text)).replace(Pattern.compile("\\{CHECKIN\\}")) { _, _ ->
				SpannableStringBuilder().bold { append(location) }
			}

		val checkoutResolvedLocation = view.findViewById<TextView>(R.id.checkout_resolved_location)
		checkoutResolvedLocation.text = location

		val checkoutResolvedTime = view.findViewById<TextView>(R.id.checkout_resolved_time)
		val start = getHourMinuteTimeString(checkinTime, ":")
		val end = getHourMinuteTimeString(checkoutTime, ":")
		checkoutResolvedTime.text = "$start – $end"

		val submitButton = view.findViewById<View>(R.id.checkout_resolved_submit_button)
		submitButton.setOnClickListener {
			dismiss()
			onCheckoutListener?.invoke()
		}
	}

	override fun onResume() {
		requireDialog().window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		super.onResume()
	}

	fun setOnCheckoutListener(onCheckoutListener: () -> Unit): CheckOutConflictResolvedDialogFragment {
		this.onCheckoutListener = onCheckoutListener
		return this
	}

}