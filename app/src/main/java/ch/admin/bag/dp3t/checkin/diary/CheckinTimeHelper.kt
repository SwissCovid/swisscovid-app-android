package ch.admin.bag.dp3t.checkin.diary

import android.content.Context
import androidx.appcompat.app.AlertDialog
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage

object CheckinTimeHelper {

	fun checkForOverlap(diaryEntry: DiaryEntry, context: Context): Boolean {
		val otherCheckins = DiaryStorage.getInstance(context).entries.filter { it.id != diaryEntry.id }
		return checkForOverlap(otherCheckins, diaryEntry.arrivalTime, diaryEntry.departureTime)
	}

	fun checkForOverlap(arrivalTime: Long, departureTime: Long, context: Context): Boolean {
		val otherCheckins = DiaryStorage.getInstance(context).entries
		return checkForOverlap(otherCheckins, arrivalTime, departureTime)
	}

	private fun checkForOverlap(checkins: List<DiaryEntry>, arrivalTime: Long, departureTime: Long): Boolean {
		return checkins.any { it.departureTime > arrivalTime && departureTime > it.arrivalTime }
	}

	fun showOverlapDialog(context: Context) {
		AlertDialog.Builder(context, R.style.NextStep_AlertDialogStyle)
			.setTitle(R.string.checkout_overlapping_alert_title)
			.setMessage(R.string.checkout_overlapping_alert_description)
			.setNegativeButton(R.string.android_button_ok) { dialog, _ -> dialog.dismiss() }
			.show()
	}


}