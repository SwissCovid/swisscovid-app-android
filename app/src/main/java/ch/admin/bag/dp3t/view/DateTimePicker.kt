package ch.admin.bag.dp3t.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import ch.admin.bag.dp3t.R
import ch.admin.bag.dp3t.databinding.ViewDatetimePickerBinding
import ch.admin.bag.dp3t.util.DateUtils
import com.shawnlin.numberpicker.NumberPicker
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

class DateTimePicker @JvmOverloads constructor(
	context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

	private val binding = ViewDatetimePickerBinding.inflate(LayoutInflater.from(context), this)

	private val zoneId = ZoneId.systemDefault()
	private val now = LocalDateTime.now()
	private var previousDateTime = now

	private val daysInPast = 180
	private val daysInFuture = 0

	private lateinit var dateFormatter: NumberPicker.Formatter
	private lateinit var hourFormatter: NumberPicker.Formatter
	private lateinit var minuteFormatter: NumberPicker.Formatter

	private var changeListener: OnDateTimeChangedListener? = null

	init {
		initializeFormatters()
		initializePickers()
		updatePickerValues()
	}

	private fun initializeFormatters() {
		val baseDate = now.toLocalDate()

		dateFormatter = NumberPicker.Formatter { value ->
			val date = baseDate.plusDays((value - daysInPast).toLong())
			if (value != daysInPast) {
				DateUtils.getFormattedDate(date.atStartOfDay(zoneId).toEpochSecond() * 1000L)
			} else {
				context.getString(R.string.date_today)
			}
		}

		hourFormatter = NumberPicker.Formatter { value ->
			String.format(Locale(context.getString(R.string.language_key)), "%02d", value)
		}
		minuteFormatter = NumberPicker.Formatter { value ->
			String.format(Locale(context.getString(R.string.language_key)), "%02d", value)
		}
	}

	private fun initializePickers() {
		val font = ResourcesCompat.getFont(context, R.font.inter_regular)

		binding.datePicker.apply {
			minValue = 0
			maxValue = daysInPast + daysInFuture
			wrapSelectorWheel = false
			formatter = dateFormatter
			typeface = font
			setSelectedTypeface(font)
			setOnValueChangedListener { _, _, _ ->
				changeListener?.onDateTimeChanged(getSelectedDateTime())
			}
		}

		binding.hourPicker.apply {
			minValue = 0
			maxValue = 23
			wrapSelectorWheel = true
			formatter = hourFormatter
			typeface = font
			setSelectedTypeface(font)
			setOnValueChangedListener { _, _, _ ->
				changeListener?.onDateTimeChanged(getSelectedDateTime())
			}
		}

		binding.minutePicker.apply {
			minValue = 0
			maxValue = 59
			wrapSelectorWheel = true
			formatter = minuteFormatter
			typeface = font
			setSelectedTypeface(font)
			setOnValueChangedListener { _, _, _ ->
				changeListener?.onDateTimeChanged(getSelectedDateTime())
			}
		}
	}

	fun setDateTime(unixTimestamp: Long) {
		setDateTime(
			Instant.ofEpochMilli(unixTimestamp)
				.atZone(zoneId)
				.toLocalDateTime()
		)
	}

	fun setDateTime(previousDateTime: LocalDateTime) {
		this.previousDateTime = previousDateTime
		updatePickerValues()
	}

	fun setOnDateTimeChangedListener(changeListener: OnDateTimeChangedListener) {
		this.changeListener = changeListener
	}

	fun getSelectedDateTime(): LocalDateTime {
		val dateValue = binding.datePicker.value - daysInPast
		val hourValue = binding.hourPicker.value
		val minuteValue = binding.minutePicker.value
		return now.plusDays(dateValue.toLong()).withHour(hourValue).withMinute(minuteValue)
	}

	fun getSelectedUnixTimestamp(): Long {
		return getSelectedDateTime().atZone(zoneId).toEpochSecond() * 1000L
	}

	private fun updatePickerValues() {
		val dayDifference = ChronoUnit.DAYS.between(now.toLocalDate(), previousDateTime.toLocalDate())
		val preSelectedValue = daysInPast + dayDifference
		binding.datePicker.value = preSelectedValue.toInt()
		binding.hourPicker.value = previousDateTime.hour
		val minuteSelectionValue = previousDateTime.minute
		binding.minutePicker.value = minuteSelectionValue
	}

	fun interface OnDateTimeChangedListener {
		fun onDateTimeChanged(newDateTime: LocalDateTime)
	}

}