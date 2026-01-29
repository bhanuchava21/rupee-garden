package com.bhanu.rupeegarden.domain.usecase

import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.SpendingCategory
import com.bhanu.rupeegarden.data.repository.EntryRepository
import java.time.YearMonth

data class MonthlySpendingResult(
    val totalSpent: Double,
    val budget: Double,
    val remainingBudget: Double,
    val isOverBudget: Boolean,
    val budgetUsedPercent: Float,
    val spendingByCategory: Map<SpendingCategory, Double>,
    val savedDays: Int,
    val spentDays: Int,
    val entries: List<DayEntry>
)

class GetMonthlySpendingUseCase(private val entryRepository: EntryRepository) {

    suspend operator fun invoke(yearMonth: YearMonth, budget: Double): MonthlySpendingResult {
        val entries = entryRepository.getEntriesForMonth(yearMonth)

        val totalSpent = entries
            .filter { it.saved == false }
            .mapNotNull { it.spentAmount }
            .sum()

        val spendingByCategory = entries
            .filter { it.saved == false && it.spentCategory != null }
            .groupBy { it.spentCategory!! }
            .mapValues { (_, entries) ->
                entries.mapNotNull { it.spentAmount }.sum()
            }

        val savedDays = entries.count { it.saved == true }
        val spentDays = entries.count { it.saved == false }

        val remainingBudget = budget - totalSpent
        val isOverBudget = totalSpent > budget
        val budgetUsedPercent = if (budget > 0) (totalSpent / budget).toFloat().coerceIn(0f, 1.5f) else 0f

        return MonthlySpendingResult(
            totalSpent = totalSpent,
            budget = budget,
            remainingBudget = remainingBudget,
            isOverBudget = isOverBudget,
            budgetUsedPercent = budgetUsedPercent,
            spendingByCategory = spendingByCategory,
            savedDays = savedDays,
            spentDays = spentDays,
            entries = entries
        )
    }
}
