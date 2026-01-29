package com.bhanu.rupeegarden.ui.screens.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.SpendingCategory
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import com.bhanu.rupeegarden.domain.usecase.GetMonthlySpendingUseCase
import com.bhanu.rupeegarden.domain.usecase.MonthlySpendingResult
import com.bhanu.rupeegarden.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth

data class InsightsUiState(
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val monthLabel: String = "",
    val spending: MonthlySpendingResult? = null,
    val canGoNext: Boolean = false,
    val canGoPrevious: Boolean = true,
    val error: String? = null
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val entryRepository = EntryRepository(dataStore)
    private val progressRepository = ProgressRepository(dataStore)
    private val getMonthlySpendingUseCase = GetMonthlySpendingUseCase(entryRepository)

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val progress = progressRepository.getCurrentProgress()
                val spending = getMonthlySpendingUseCase(
                    _uiState.value.selectedMonth,
                    progress.monthlyBudget
                )

                val currentMonth = YearMonth.now()
                val selectedMonth = _uiState.value.selectedMonth

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        spending = spending,
                        monthLabel = DateUtils.getMonthYear(selectedMonth),
                        canGoNext = selectedMonth.isBefore(currentMonth),
                        canGoPrevious = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun previousMonth() {
        _uiState.update {
            it.copy(selectedMonth = it.selectedMonth.minusMonths(1))
        }
        loadData()
    }

    fun nextMonth() {
        val currentMonth = YearMonth.now()
        if (_uiState.value.selectedMonth.isBefore(currentMonth)) {
            _uiState.update {
                it.copy(selectedMonth = it.selectedMonth.plusMonths(1))
            }
            loadData()
        }
    }

    fun getCategorySpending(): List<Pair<SpendingCategory, Double>> {
        return _uiState.value.spending?.spendingByCategory
            ?.toList()
            ?.sortedByDescending { it.second }
            ?: emptyList()
    }
}
