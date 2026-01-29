package com.bhanu.rupeegarden.ui.screens.achievements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.Achievement
import com.bhanu.rupeegarden.data.repository.AchievementRepository
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth

data class AchievementsUiState(
    val isLoading: Boolean = true,
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0
)

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val achievementRepository = AchievementRepository(dataStore)
    private val progressRepository = ProgressRepository(dataStore)
    private val entryRepository = EntryRepository(dataStore)

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        checkAndLoadAchievements()
    }

    private fun checkAndLoadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Get current progress
            val progress = progressRepository.getCurrentProgress()

            // Get entries this month for garden achievement
            val entriesThisMonth = entryRepository.getEntriesForMonth(YearMonth.now()).size

            // Check and unlock any new achievements
            achievementRepository.checkAndUnlockAchievements(progress, entriesThisMonth)

            // Load all achievements with status
            val allAchievements = achievementRepository.getAllAchievementsWithStatus()
            val unlockedCount = allAchievements.count { it.isUnlocked }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    achievements = allAchievements,
                    unlockedCount = unlockedCount,
                    totalCount = allAchievements.size
                )
            }
        }
    }
}
