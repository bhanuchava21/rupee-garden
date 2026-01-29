package com.bhanu.rupeegarden.data.repository

import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.Achievement
import com.bhanu.rupeegarden.data.model.AchievementType
import com.bhanu.rupeegarden.data.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AchievementRepository(private val dataStore: RupeeGardenDataStore) {

    val achievements: Flow<List<Achievement>> = dataStore.achievements

    suspend fun getAchievements(): List<Achievement> {
        return dataStore.achievements.first()
    }

    suspend fun checkAndUnlockAchievements(progress: UserProgress, entriesThisMonth: Int): List<Achievement> {
        val currentAchievements = getAchievements().toMutableList()
        val newlyUnlocked = mutableListOf<Achievement>()
        val now = System.currentTimeMillis()

        // Helper to unlock achievement
        fun unlock(type: AchievementType) {
            val existing = currentAchievements.find { it.id == type.id }
            if (existing == null || !existing.isUnlocked) {
                val unlocked = type.toAchievement(now)
                if (existing != null) {
                    currentAchievements.remove(existing)
                }
                currentAchievements.add(unlocked)
                newlyUnlocked.add(unlocked)
            }
        }

        // Check streak achievements
        if (progress.totalSavedDays >= 1) unlock(AchievementType.FIRST_SAVE)
        if (progress.longestStreak >= 7) unlock(AchievementType.WEEK_WARRIOR)
        if (progress.longestStreak >= 30) unlock(AchievementType.MONTH_MASTER)
        if (progress.longestStreak >= 100) unlock(AchievementType.CENTURY_SAVER)

        // Check level achievements
        if (progress.level >= 5) unlock(AchievementType.LEVEL_5)
        if (progress.level >= 10) unlock(AchievementType.LEVEL_10)
        if (progress.level >= 25) unlock(AchievementType.LEVEL_25)
        if (progress.level >= 50) unlock(AchievementType.LEVEL_50)

        // Check total saves achievements
        if (progress.totalSavedDays >= 10) unlock(AchievementType.SAVED_10)
        if (progress.totalSavedDays >= 50) unlock(AchievementType.SAVED_50)
        if (progress.totalSavedDays >= 100) unlock(AchievementType.SAVED_100)

        // Check XP achievements
        if (progress.totalXp >= 1000) unlock(AchievementType.XP_1000)
        if (progress.totalXp >= 5000) unlock(AchievementType.XP_5000)
        if (progress.totalXp >= 10000) unlock(AchievementType.XP_10000)

        // Check garden achievements
        if (progress.totalDays >= 1) unlock(AchievementType.FIRST_TREE)
        if (entriesThisMonth >= 16) unlock(AchievementType.FULL_GARDEN)

        // Save updated achievements
        if (newlyUnlocked.isNotEmpty()) {
            dataStore.saveAchievements(currentAchievements)
        }

        return newlyUnlocked
    }

    suspend fun getAllAchievementsWithStatus(): List<Achievement> {
        val unlocked = getAchievements().associateBy { it.id }
        return AchievementType.entries.map { type ->
            unlocked[type.id] ?: type.toAchievement(null)
        }
    }
}
