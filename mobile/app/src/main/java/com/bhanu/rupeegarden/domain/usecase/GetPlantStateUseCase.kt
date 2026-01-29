package com.bhanu.rupeegarden.domain.usecase

import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.model.PlantState

class GetPlantStateUseCase {

    operator fun invoke(entry: DayEntry): PlantState {
        return if (entry.isCompleted) {
            // Completed entries show based on saved/spent
            if (entry.saved == true) PlantState.FULL else PlantState.WITHERED
        } else {
            // Active session - calculate based on elapsed time
            val elapsedSeconds = (System.currentTimeMillis() - entry.startedAt) / 1000
            getPlantStateFromTime(elapsedSeconds)
        }
    }

    fun getPlantStateFromTime(elapsedSeconds: Long): PlantState {
        return when {
            elapsedSeconds < 5 -> PlantState.SEED
            elapsedSeconds < 15 -> PlantState.SPROUT
            elapsedSeconds < 30 -> PlantState.YOUNG
            else -> PlantState.FULL
        }
    }

    fun getProgressToNextState(elapsedSeconds: Long): Float {
        return when {
            elapsedSeconds < 5 -> elapsedSeconds / 5f
            elapsedSeconds < 15 -> (elapsedSeconds - 5) / 10f
            elapsedSeconds < 30 -> (elapsedSeconds - 15) / 15f
            else -> 1f
        }
    }

    fun getSecondsToNextState(elapsedSeconds: Long): Long {
        return when {
            elapsedSeconds < 5 -> 5 - elapsedSeconds
            elapsedSeconds < 15 -> 15 - elapsedSeconds
            elapsedSeconds < 30 -> 30 - elapsedSeconds
            else -> 0
        }
    }
}
