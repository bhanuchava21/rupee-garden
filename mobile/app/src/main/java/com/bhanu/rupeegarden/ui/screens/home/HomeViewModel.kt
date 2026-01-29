package com.bhanu.rupeegarden.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.ImpulseStats
import com.bhanu.rupeegarden.data.model.UserProgress
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.data.repository.ImpulseRepository
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import com.bhanu.rupeegarden.domain.usecase.GetMonthlySpendingUseCase
import com.bhanu.rupeegarden.domain.usecase.HandleStaleSessionUseCase
import com.bhanu.rupeegarden.domain.usecase.MonthlySpendingResult
import com.bhanu.rupeegarden.domain.usecase.StaleSessionResult
import com.bhanu.rupeegarden.domain.usecase.StartSessionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth

data class HomeUiState(
    val isLoading: Boolean = true,
    val progress: UserProgress = UserProgress(),
    val monthlySpending: MonthlySpendingResult? = null,
    val hasActiveSession: Boolean = false,
    val activeSession: DayEntry? = null,
    val canStartNewSession: Boolean = false,
    val error: String? = null,
    val navigateToSession: Boolean = false,
    val impulseStats: ImpulseStats = ImpulseStats()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val entryRepository = EntryRepository(dataStore)
    private val progressRepository = ProgressRepository(dataStore)
    private val impulseRepository = ImpulseRepository(dataStore)
    private val startSessionUseCase = StartSessionUseCase(entryRepository)
    private val handleStaleSessionUseCase = HandleStaleSessionUseCase(entryRepository, progressRepository)
    private val getMonthlySpendingUseCase = GetMonthlySpendingUseCase(entryRepository)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Handle any stale sessions first
                val staleResult = handleStaleSessionUseCase()

                // Load progress
                val progress = progressRepository.getCurrentProgress()

                // Load monthly spending
                val yearMonth = YearMonth.now()
                val monthlySpending = getMonthlySpendingUseCase(yearMonth, progress.monthlyBudget)

                // Load impulse stats
                val impulseStats = impulseRepository.getImpulseStats()

                // Check for active session
                val activeSession = when (staleResult) {
                    is StaleSessionResult.ActiveSession -> staleResult.entry
                    else -> null
                }

                val hasEntryForToday = entryRepository.hasEntryForToday()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        progress = progress,
                        monthlySpending = monthlySpending,
                        hasActiveSession = activeSession != null,
                        activeSession = activeSession,
                        canStartNewSession = !hasEntryForToday,
                        impulseStats = impulseStats
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun startSession() {
        viewModelScope.launch {
            val result = startSessionUseCase()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        hasActiveSession = true,
                        activeSession = result.getOrNull(),
                        canStartNewSession = false,
                        navigateToSession = true
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(error = error.message)
                }
            }
        }
    }

    fun onNavigatedToSession() {
        _uiState.update { it.copy(navigateToSession = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
