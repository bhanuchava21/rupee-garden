package com.bhanu.rupeegarden.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import com.bhanu.rupeegarden.util.DemoDataSeeder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val currentBudget: Double = 10000.0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val demoDataSeeder = DemoDataSeeder(dataStore)
    private val progressRepository = ProgressRepository(dataStore)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentBudget()
    }

    private fun loadCurrentBudget() {
        viewModelScope.launch {
            val progress = progressRepository.getCurrentProgress()
            _uiState.update { it.copy(currentBudget = progress.monthlyBudget) }
        }
    }

    fun updateBudget(budget: Double) {
        viewModelScope.launch {
            try {
                progressRepository.updateBudget(budget)
                _uiState.update {
                    it.copy(
                        currentBudget = budget,
                        message = "Budget updated to ₹${String.format("%,.0f", budget)}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(message = "Error updating budget: ${e.message}")
                }
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                demoDataSeeder.seedDemoData()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Demo data loaded successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                demoDataSeeder.clearAllData()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "All data cleared!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
