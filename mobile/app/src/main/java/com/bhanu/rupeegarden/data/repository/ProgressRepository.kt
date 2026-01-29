package com.bhanu.rupeegarden.data.repository

import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ProgressRepository(private val dataStore: RupeeGardenDataStore) {

    val userProgress: Flow<UserProgress> = dataStore.userProgress

    suspend fun getCurrentProgress(): UserProgress {
        return dataStore.userProgress.first()
    }

    suspend fun addXp(xp: Int, date: LocalDate, saved: Boolean): UserProgress {
        val current = getCurrentProgress()
        val today = date.toString()

        // Calculate streak
        val (newStreak, longestStreak) = calculateStreak(current, date, saved)

        val updated = current.copy(
            totalXp = current.totalXp + xp,
            currentStreak = newStreak,
            longestStreak = maxOf(current.longestStreak, longestStreak),
            totalSavedDays = if (saved) current.totalSavedDays + 1 else current.totalSavedDays,
            totalSpentDays = if (!saved) current.totalSpentDays + 1 else current.totalSpentDays,
            lastEntryDate = today
        )

        dataStore.saveUserProgress(updated)
        return updated
    }

    private fun calculateStreak(current: UserProgress, date: LocalDate, saved: Boolean): Pair<Int, Int> {
        if (!saved) {
            // Spending resets streak
            return Pair(0, current.longestStreak)
        }

        val lastDate = current.lastEntryDate?.let { LocalDate.parse(it) }

        return if (lastDate == null) {
            // First entry ever
            Pair(1, 1)
        } else {
            val daysBetween = ChronoUnit.DAYS.between(lastDate, date)
            when {
                daysBetween == 1L -> {
                    // Consecutive day
                    val newStreak = current.currentStreak + 1
                    Pair(newStreak, maxOf(current.longestStreak, newStreak))
                }
                daysBetween == 0L -> {
                    // Same day (shouldn't happen, but handle it)
                    Pair(current.currentStreak, current.longestStreak)
                }
                else -> {
                    // Gap in days, streak resets
                    Pair(1, current.longestStreak)
                }
            }
        }
    }

    suspend fun updateBudget(budget: Double) {
        val current = getCurrentProgress()
        dataStore.saveUserProgress(current.copy(monthlyBudget = budget))
    }

    suspend fun saveProgress(progress: UserProgress) {
        dataStore.saveUserProgress(progress)
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        val current = getCurrentProgress()
        dataStore.saveUserProgress(current.copy(soundEnabled = enabled))
    }
}
