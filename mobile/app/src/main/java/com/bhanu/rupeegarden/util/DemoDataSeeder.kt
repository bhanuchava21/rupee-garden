package com.bhanu.rupeegarden.util

import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.SpendingCategory
import com.bhanu.rupeegarden.data.model.UserProgress
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlin.random.Random

class DemoDataSeeder(private val dataStore: RupeeGardenDataStore) {

    suspend fun seedDemoData() {
        val entries = mutableListOf<DayEntry>()

        // Current month (Jan 2026) - 25 days
        val currentMonth = YearMonth.now()
        entries.addAll(generateEntriesForMonth(currentMonth, 25))

        // December 2025 - full month
        val dec2025 = YearMonth.of(2025, 12)
        entries.addAll(generateEntriesForMonth(dec2025, 28))

        // November 2025 - full month
        val nov2025 = YearMonth.of(2025, 11)
        entries.addAll(generateEntriesForMonth(nov2025, 27))

        // October 2025 - partial month
        val oct2025 = YearMonth.of(2025, 10)
        entries.addAll(generateEntriesForMonth(oct2025, 20))

        // Calculate stats
        var totalXp = 0
        var savedDays = 0
        var spentDays = 0

        entries.forEach { entry ->
            totalXp += entry.xpEarned
            if (entry.saved == true) savedDays++ else spentDays++
        }

        val currentStreak = calculateCurrentStreak(entries)
        val longestStreak = calculateLongestStreak(entries)

        val progress = UserProgress(
            totalXp = totalXp,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalSavedDays = savedDays,
            totalSpentDays = spentDays,
            monthlyBudget = 10000.0,
            lastEntryDate = entries.maxByOrNull { it.date }?.date
        )

        dataStore.saveEntries(entries)
        dataStore.saveUserProgress(progress)
    }

    suspend fun clearAllData() {
        dataStore.saveEntries(emptyList())
        dataStore.saveUserProgress(UserProgress())
        dataStore.clearActiveSession()
    }

    suspend fun hasData(): Boolean {
        return try {
            val entries = dataStore.entries.first()
            entries.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun generateEntriesForMonth(yearMonth: YearMonth, count: Int): List<DayEntry> {
        val entries = mutableListOf<DayEntry>()
        val daysInMonth = yearMonth.lengthOfMonth()
        val today = LocalDate.now()

        // Generate random days to have entries
        val daysWithEntries = (1..daysInMonth)
            .shuffled()
            .take(count.coerceAtMost(daysInMonth))
            .sorted()

        daysWithEntries.forEach { day ->
            val date = yearMonth.atDay(day)

            // Skip future dates
            if (date.isAfter(today)) return@forEach

            // 70% chance of saving
            val saved = Random.nextFloat() < 0.7f

            val entry = if (saved) {
                DayEntry(
                    id = UUID.randomUUID().toString(),
                    date = date.toString(),
                    startedAt = date.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    completedAt = date.atStartOfDay().plusHours(1).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    saved = true,
                    xpEarned = XpCalculator.getTotalXpForDay(true)
                )
            } else {
                val category = SpendingCategory.entries.random()
                val amount = Random.nextDouble(100.0, 2000.0)

                DayEntry(
                    id = UUID.randomUUID().toString(),
                    date = date.toString(),
                    startedAt = date.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    completedAt = date.atStartOfDay().plusHours(1).toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    saved = false,
                    spentAmount = amount,
                    spentCategory = category,
                    spentDescription = getRandomDescription(category),
                    xpEarned = XpCalculator.getTotalXpForDay(false)
                )
            }

            entries.add(entry)
        }

        return entries
    }

    private fun calculateCurrentStreak(entries: List<DayEntry>): Int {
        val sortedEntries = entries.sortedByDescending { it.date }
        var streak = 0

        for (entry in sortedEntries) {
            if (entry.saved == true) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateLongestStreak(entries: List<DayEntry>): Int {
        val sortedEntries = entries.sortedBy { it.date }
        var longestStreak = 0
        var currentStreak = 0

        for (entry in sortedEntries) {
            if (entry.saved == true) {
                currentStreak++
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
            } else {
                currentStreak = 0
            }
        }

        return longestStreak
    }

    private fun getRandomDescription(category: SpendingCategory): String {
        return when (category) {
            SpendingCategory.FOOD -> listOf("Lunch", "Coffee", "Dinner with friends", "Groceries", "Snacks").random()
            SpendingCategory.TRANSPORT -> listOf("Uber ride", "Metro fare", "Fuel", "Auto rickshaw", "Cab to office").random()
            SpendingCategory.SHOPPING -> listOf("New shoes", "Clothes", "Electronics", "Home decor", "Books").random()
            SpendingCategory.ENTERTAINMENT -> listOf("Movie tickets", "Netflix", "Concert", "Gaming", "Spotify").random()
            SpendingCategory.BILLS -> listOf("Phone bill", "Internet", "Electricity", "Water bill", "Gas").random()
            SpendingCategory.HEALTH -> listOf("Medicine", "Doctor visit", "Gym", "Vitamins", "Health checkup").random()
            SpendingCategory.OTHER -> listOf("Gift", "Donation", "Misc", "Emergency", "Subscription").random()
        }
    }
}
