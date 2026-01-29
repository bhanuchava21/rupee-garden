package com.bhanu.rupeegarden.data.repository

import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.SpendingCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class EntryRepository(private val dataStore: RupeeGardenDataStore) {

    val entries: Flow<List<DayEntry>> = dataStore.entries
    val activeSession: Flow<DayEntry?> = dataStore.activeSession

    suspend fun createEntry(date: LocalDate): DayEntry {
        val entry = DayEntry(
            id = UUID.randomUUID().toString(),
            date = date.toString(),
            startedAt = System.currentTimeMillis(),
            xpEarned = 5 // Start session XP
        )
        dataStore.saveActiveSession(entry)
        return entry
    }

    suspend fun completeEntry(
        entry: DayEntry,
        saved: Boolean,
        spentAmount: Double? = null,
        spentCategory: SpendingCategory? = null,
        spentDescription: String? = null,
        additionalXp: Int
    ): DayEntry {
        val completedEntry = entry.copy(
            completedAt = System.currentTimeMillis(),
            saved = saved,
            spentAmount = spentAmount,
            spentCategory = spentCategory,
            spentDescription = spentDescription,
            xpEarned = entry.xpEarned + additionalXp
        )

        // Add to entries list
        val currentEntries = dataStore.entries.first().toMutableList()
        currentEntries.add(completedEntry)
        dataStore.saveEntries(currentEntries)

        // Clear active session
        dataStore.clearActiveSession()

        return completedEntry
    }

    suspend fun getEntriesForMonth(yearMonth: YearMonth): List<DayEntry> {
        return dataStore.entries.first().filter { entry ->
            val entryDate = LocalDate.parse(entry.date)
            YearMonth.from(entryDate) == yearMonth
        }
    }

    suspend fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): List<DayEntry> {
        return dataStore.entries.first().filter { entry ->
            val entryDate = LocalDate.parse(entry.date)
            !entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)
        }
    }

    suspend fun getEntryForDate(date: LocalDate): DayEntry? {
        return dataStore.entries.first().find { it.date == date.toString() }
    }

    suspend fun hasEntryForToday(): Boolean {
        val today = LocalDate.now().toString()
        val entries = dataStore.entries.first()
        val activeSession = dataStore.activeSession.first()
        return entries.any { it.date == today } || activeSession?.date == today
    }

    suspend fun getActiveSession(): DayEntry? {
        return dataStore.activeSession.first()
    }

    suspend fun saveEntries(entries: List<DayEntry>) {
        dataStore.saveEntries(entries)
    }
}
