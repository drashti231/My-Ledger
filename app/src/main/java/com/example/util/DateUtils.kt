package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val standardDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.US)
    private val monthFormat = SimpleDateFormat("MMM", Locale.US)

    fun formatDate(timestampMillis: Long): String {
        return standardDateFormat.format(Date(timestampMillis))
    }

    fun formatShortDate(timestampMillis: Long): String {
        return shortDateFormat.format(Date(timestampMillis))
    }

    fun formatMonthYear(timestampMillis: Long): String {
        return monthYearFormat.format(Date(timestampMillis))
    }

    fun formatMonth(timestampMillis: Long): String {
        return monthFormat.format(Date(timestampMillis))
    }

    enum class DateFilter(val label: String) {
        THIS_WEEK("This Week"),
        THIS_MONTH("This Month"),
        LAST_MONTH("Last Month"),
        THIS_YEAR("This Year"),
        ALL_TIME("All Time")
    }

    fun getTimeRange(filter: DateFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (filter) {
            DateFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, now + 86400000L)
            }
            DateFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, now + 86400000L)
            }
            DateFilter.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            DateFilter.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, now + 86400000L)
            }
            DateFilter.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
