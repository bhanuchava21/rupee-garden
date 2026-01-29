package com.bhanu.rupeegarden.ui.screens.enddaycheck

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.SpendingCategory
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import com.bhanu.rupeegarden.domain.usecase.CompleteDayResult
import com.bhanu.rupeegarden.domain.usecase.CompleteDayUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EndDayCheckUiState(
    val isLoading: Boolean = true,
    val activeSession: DayEntry? = null,
    val showSpendingForm: Boolean = false,
    val selectedCategory: SpendingCategory? = null,
    val spentAmount: String = "",
    val spentDescription: String = "",
    val isCompleting: Boolean = false,
    val completionResult: CompleteDayResult? = null,
    val error: String? = null
)

class EndDayCheckViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val entryRepository = EntryRepository(dataStore)
    private val progressRepository = ProgressRepository(dataStore)
    private val completeDayUseCase = CompleteDayUseCase(entryRepository, progressRepository)

    private val _uiState = MutableStateFlow(EndDayCheckUiState())
    val uiState: StateFlow<EndDayCheckUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val session = entryRepository.getActiveSession()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeSession = session,
                        error = if (session == null) "No active session" else null
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

    fun onSavedSelected() {
        completeDayAsSaved()
    }

    fun onSpentSelected() {
        _uiState.update { it.copy(showSpendingForm = true) }
    }

    fun onCategorySelected(category: SpendingCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onAmountChanged(amount: String) {
        // Only allow numbers and decimal point
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(spentAmount = filtered) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(spentDescription = description) }
    }

    fun onBackFromSpendingForm() {
        _uiState.update {
            it.copy(
                showSpendingForm = false,
                selectedCategory = null,
                spentAmount = "",
                spentDescription = ""
            )
        }
    }

    fun completeDayAsSpent() {
        val currentState = _uiState.value
        val session = currentState.activeSession ?: return
        val category = currentState.selectedCategory ?: return
        val amount = currentState.spentAmount.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            val result = completeDayUseCase(
                entry = session,
                saved = false,
                spentAmount = amount,
                spentCategory = category,
                spentDescription = currentState.spentDescription.takeIf { it.isNotBlank() }
            )

            result.onSuccess { completionResult ->
                _uiState.update {
                    it.copy(
                        isCompleting = false,
                        completionResult = completionResult
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCompleting = false,
                        error = error.message
                    )
                }
            }
        }
    }

    private fun completeDayAsSaved() {
        val session = _uiState.value.activeSession ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true) }

            val result = completeDayUseCase(
                entry = session,
                saved = true
            )

            result.onSuccess { completionResult ->
                _uiState.update {
                    it.copy(
                        isCompleting = false,
                        completionResult = completionResult
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCompleting = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun canSubmitSpending(): Boolean {
        val state = _uiState.value
        return state.selectedCategory != null &&
                state.spentAmount.isNotBlank() &&
                state.spentAmount.toDoubleOrNull() != null &&
                state.spentAmount.toDoubleOrNull()!! > 0
    }
}
