package com.bhanu.rupeegarden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSavedDays: Int = 0,
    val totalSpentDays: Int = 0,
    val monthlyBudget: Double = 10000.0, // Default budget in rupees
    val lastEntryDate: String? = null // ISO format for streak calculation
) {
    val level: Int get() = (totalXp / 200) + 1
    val xpInCurrentLevel: Int get() = totalXp % 200
    val xpToNextLevel: Int get() = 200 - xpInCurrentLevel
    val totalDays: Int get() = totalSavedDays + totalSpentDays
}
