package com.bhanu.rupeegarden.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val shortFormatter = DateTimeFormatter.ofPattern("MMM d")

    fun formatDisplayDate(date: LocalDate): String {
        return date.format(displayFormatter)
    }

    fun formatShortDate(date: LocalDate): String {
        return date.format(shortFormatter)
    }

    fun getMonthName(yearMonth: YearMonth): String {
        return yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    fun getMonthYear(yearMonth: YearMonth): String {
        return "${getMonthName(yearMonth)} ${yearMonth.year}"
    }

    fun isToday(date: LocalDate): Boolean {
        return date == LocalDate.now()
    }

    fun isYesterday(date: LocalDate): Boolean {
        return date == LocalDate.now().minusDays(1)
    }

    fun getRelativeDay(date: LocalDate): String {
        return when {
            isToday(date) -> "Today"
            isYesterday(date) -> "Yesterday"
            else -> formatShortDate(date)
        }
    }

    fun getDaysInMonth(yearMonth: YearMonth): List<LocalDate> {
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        return generateSequence(firstDay) { it.plusDays(1) }
            .takeWhile { !it.isAfter(lastDay) }
            .toList()
    }

    fun getWeekDates(referenceDate: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> {
        val startOfWeek = referenceDate.minusDays(referenceDate.dayOfWeek.value.toLong() - 1)
        val endOfWeek = startOfWeek.plusDays(6)
        return Pair(startOfWeek, endOfWeek)
    }
}
