package com.bhanu.rupeegarden.domain.usecase

import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.SpendingCategory
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import com.bhanu.rupeegarden.util.XpCalculator
import java.time.LocalDate

data class CompleteDayResult(
    val entry: DayEntry,
    val xpEarned: Int,
    val newTotalXp: Int,
    val leveledUp: Boolean,
    val newLevel: Int,
    val newStreak: Int
)

class CompleteDayUseCase(
    private val entryRepository: EntryRepository,
    private val progressRepository: ProgressRepository
) {

    suspend operator fun invoke(
        entry: DayEntry,
        saved: Boolean,
        spentAmount: Double? = null,
        spentCategory: SpendingCategory? = null,
        spentDescription: String? = null
    ): Result<CompleteDayResult> {
        return try {
            val previousProgress = progressRepository.getCurrentProgress()
            val previousLevel = previousProgress.level

            // Calculate XP for completion
            val completionXp = XpCalculator.getCompletionXp(saved)

            // Complete the entry
            val completedEntry = entryRepository.completeEntry(
                entry = entry,
                saved = saved,
                spentAmount = spentAmount,
                spentCategory = spentCategory,
                spentDescription = spentDescription,
                additionalXp = completionXp
            )

            // Update progress
            val date = LocalDate.parse(entry.date)
            val updatedProgress = progressRepository.addXp(
                xp = completionXp,
                date = date,
                saved = saved
            )

            val leveledUp = updatedProgress.level > previousLevel

            Result.success(
                CompleteDayResult(
                    entry = completedEntry,
                    xpEarned = completedEntry.xpEarned,
                    newTotalXp = updatedProgress.totalXp,
                    leveledUp = leveledUp,
                    newLevel = updatedProgress.level,
                    newStreak = updatedProgress.currentStreak
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
