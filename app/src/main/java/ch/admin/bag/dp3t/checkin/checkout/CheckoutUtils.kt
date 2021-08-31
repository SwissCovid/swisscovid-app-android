package ch.admin.bag.dp3t.checkin.checkout

import ch.admin.bag.dp3t.checkin.models.CheckinInfo
import ch.admin.bag.dp3t.checkin.models.DiaryEntry
import ch.admin.bag.dp3t.checkin.storage.DiaryStorage

fun DiaryStorage.checkForOverlap(diaryEntry: CheckinInfo): Collection<DiaryEntry> {
	val otherCheckins = this.entries.filter { it.id != diaryEntry.id }
	return otherCheckins.filter { it.checkOutTime > diaryEntry.checkInTime && diaryEntry.checkOutTime > it.checkInTime }
}
