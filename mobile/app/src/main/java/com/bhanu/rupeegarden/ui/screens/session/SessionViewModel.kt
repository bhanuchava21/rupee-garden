package com.bhanu.rupeegarden.ui.screens.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.PlantState
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.domain.usecase.GetPlantStateUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionUiState(
    val isLoading: Boolean = true,
    val activeSession: DayEntry? = null,
    val elapsedSeconds: Long = 0,
    val plantState: PlantState = PlantState.SEED,
    val isFullGrown: Boolean = false,
    val error: String? = null
)

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val entryRepository = EntryRepository(dataStore)
    private val getPlantStateUseCase = GetPlantStateUseCase()

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        loadSession()
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val session = entryRepository.getActiveSession()

                if (session != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeSession = session
                        )
                    }
                    startTimer(session)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No active session found"
                        )
                    }
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

    private fun startTimer(session: DayEntry) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = (System.currentTimeMillis() - session.startedAt) / 1000
                val plantState = getPlantStateUseCase.getPlantStateFromTime(elapsed)
                val isFullGrown = plantState == PlantState.FULL

                _uiState.update {
                    it.copy(
                        elapsedSeconds = elapsed,
                        plantState = plantState,
                        isFullGrown = isFullGrown
                    )
                }

                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun getActiveSession(): DayEntry? {
        return _uiState.value.activeSession
    }
}
