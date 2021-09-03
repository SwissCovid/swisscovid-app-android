package ch.admin.bag.dp3t.checkin.checkinflow

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import ch.admin.bag.dp3t.R

class AlreadyCheckedInErrorDialog(context: Context) : AlertDialog(context) {

	private var onCheckoutListener: (() -> Unit)? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.dialog_already_checkedin)

		val closeButton = findViewById<View>(R.id.already_checkedin_close_button)
		closeButton?.setOnClickListener { dismiss() }

		val checkoutButton = findViewById<View>(R.id.already_checkedin_checkout_button)
		checkoutButton?.setOnClickListener {
			dismiss()
			onCheckoutListener?.invoke()
		}

		val cancelButton = findViewById<View>(R.id.already_checkedin_cancel_button)
		cancelButton?.setOnClickListener { dismiss() }

		window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		window?.setBackgroundDrawableResource(R.drawable.dialog_background)
	}

	fun setOnCheckoutListener(onCheckoutListener: () -> Unit): AlreadyCheckedInErrorDialog {
		this.onCheckoutListener = onCheckoutListener
		return this
	}

}