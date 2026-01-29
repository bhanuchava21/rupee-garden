package com.bhanu.rupeegarden.ui.screens.impulse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.ImpulseEntry
import com.bhanu.rupeegarden.data.model.ImpulseResult
import com.bhanu.rupeegarden.data.model.ImpulseVerdict
import com.bhanu.rupeegarden.data.repository.ImpulseRepository
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import com.bhanu.rupeegarden.util.ImpulseScoreCalculator
import com.bhanu.rupeegarden.util.XpCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class ImpulseCheckStep {
    BREATHING,      // 30-second timer
    FIRST_CHECK,    // "Do you still want to buy?"
    QUESTIONS,      // 2-3 questions (only if still considering)
    FEEDBACK,       // Language feedback (no score)
    FINAL_CHECK     // "I resisted" / "I still spent"
}

data class ImpulseCheckUiState(
    val currentStep: ImpulseCheckStep = ImpulseCheckStep.BREATHING,
    val timerSeconds: Int = 30,
    val isTimerComplete: Boolean = false,
    // Questions
    val isEssential: Boolean? = null,
    val ownsSimilar: Boolean? = null,
    val canWait: Boolean? = null,
    // Internal score (not shown to user)
    val impulseScore: Int = 0,
    val verdict: ImpulseVerdict = ImpulseVerdict.MAYBE_WAIT,
    val feedbackMessage: String = "",
    // Result
    val result: ImpulseResult? = null,
    val isComplete: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val areAllQuestionsAnswered: Boolean
        get() = isEssential != null && ownsSimilar != null && canWait != null
}

class ImpulseCheckViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val impulseRepository = ImpulseRepository(dataStore)
    private val progressRepository = ProgressRepository(dataStore)

    private val _uiState = MutableStateFlow(ImpulseCheckUiState())
    val uiState: StateFlow<ImpulseCheckUiState> = _uiState.asStateFlow()

    init {
        startBreathingTimer()
    }

    private fun startBreathingTimer() {
        viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0) {
                delay(1000)
                _uiState.update {
                    val newSeconds = it.timerSeconds - 1
                    it.copy(
                        timerSeconds = newSeconds,
                        isTimerComplete = newSeconds <= 0
                    )
                }
            }
        }
    }

    fun onTimerComplete() {
        _uiState.update { it.copy(currentStep = ImpulseCheckStep.FIRST_CHECK) }
    }

    // First check: "Do you still want to buy?"
    fun onResistedEarly() {
        // User resisted after breathing - great!
        completeFlow(resisted = true)
    }

    fun onStillConsidering() {
        // User still wants to buy - show questions
        _uiState.update { it.copy(currentStep = ImpulseCheckStep.QUESTIONS) }
    }

    // Questions
    fun updateIsEssential(value: Boolean) {
        _uiState.update { it.copy(isEssential = value) }
    }

    fun updateOwnsSimilar(value: Boolean) {
        _uiState.update { it.copy(ownsSimilar = value) }
    }

    fun updateCanWait(value: Boolean) {
        _uiState.update { it.copy(canWait = value) }
    }

    fun onQuestionsComplete() {
        val state = _uiState.value
        if (!state.areAllQuestionsAnswered) return

        // Calculate score internally (not shown to user)
        val score = ImpulseScoreCalculator.calculateScore(
            isEssential = state.isEssential!!,
            ownsSimilar = state.ownsSimilar!!,
            currentBroken = false, // Removed this question for simplicity
            canWait = state.canWait!!
        )
        val verdict = ImpulseScoreCalculator.getVerdict(score)

        // Gentle, non-judgmental feedback
        val feedbackMessage = when (verdict) {
            ImpulseVerdict.GO_AHEAD -> "This seems like something you need."
            ImpulseVerdict.MAYBE_WAIT -> "This can probably wait."
            ImpulseVerdict.STRONG_NO -> "This looks like an impulse spend."
        }

        _uiState.update {
            it.copy(
                currentStep = ImpulseCheckStep.FEEDBACK,
                impulseScore = score,
                verdict = verdict,
                feedbackMessage = feedbackMessage
            )
        }
    }

    fun onFeedbackAcknowledged() {
        _uiState.update { it.copy(currentStep = ImpulseCheckStep.FINAL_CHECK) }
    }

    // Final check
    fun onFinalResisted() {
        completeFlow(resisted = true)
    }

    fun onFinalSpent() {
        completeFlow(resisted = false)
    }

    private fun completeFlow(resisted: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val state = _uiState.value
                // XP is added silently - user doesn't see this
                val xpEarned = XpCalculator.getImpulseXp(resisted)
                val result = if (resisted) ImpulseResult.RESISTED else ImpulseResult.BOUGHT

                val entry = ImpulseEntry(
                    id = UUID.randomUUID().toString(),
                    itemName = "", // Not collecting this anymore
                    amount = 0.0, // Not collecting this anymore
                    timestamp = System.currentTimeMillis(),
                    isEssential = state.isEssential ?: false,
                    ownsSimilar = state.ownsSimilar ?: false,
                    currentBroken = false,
                    canWait = state.canWait ?: true,
                    impulseScore = state.impulseScore,
                    verdict = state.verdict,
                    result = result,
                    xpEarned = xpEarned
                )

                impulseRepository.addImpulseEntry(entry)

                // Add XP silently to user progress
                val currentProgress = progressRepository.getCurrentProgress()
                progressRepository.saveProgress(
                    currentProgress.copy(totalXp = currentProgress.totalXp + xpEarned)
                )

                _uiState.update {
                    it.copy(
                        result = result,
                        isComplete = true,
                        isLoading = false
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
