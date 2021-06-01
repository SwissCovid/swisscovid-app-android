package ch.admin.bag.dp3t.inform.models

import ch.admin.bag.dp3t.checkin.models.DiaryEntry

data class SelectableCheckinItem(val diaryEntry: DiaryEntry, var isSelected: Boolean)