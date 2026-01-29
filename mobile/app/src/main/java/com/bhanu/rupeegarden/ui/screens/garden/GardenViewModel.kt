package com.bhanu.rupeegarden.ui.screens.garden

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.ui.components.FilterPeriod
import com.bhanu.rupeegarden.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class GardenUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: FilterPeriod = FilterPeriod.MONTH,
    val selectedMonth: YearMonth = YearMonth.now(),
    val entries: List<DayEntry> = emptyList(),
    val selectedEntry: DayEntry? = null,
    val periodLabel: String = "",
    val canGoNext: Boolean = false,
    val canGoPrevious: Boolean = true,
    val error: String? = null
)

class GardenViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = RupeeGardenDataStore(application)
    private val entryRepository = EntryRepository(dataStore)

    private val _uiState = MutableStateFlow(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val selectedMonth = _uiState.value.selectedMonth
                val today = LocalDate.now()
                val currentMonth = YearMonth.now()

                val entries = when (_uiState.value.selectedPeriod) {
                    FilterPeriod.DAY -> {
                        if (selectedMonth == currentMonth) {
                            entryRepository.getEntriesForDateRange(today, today)
                        } else {
                            // For past months in day view, show last day of that month
                            val lastDay = selectedMonth.atEndOfMonth()
                            entryRepository.getEntriesForDateRange(lastDay, lastDay)
                        }
                    }
                    FilterPeriod.WEEK -> {
                        if (selectedMonth == currentMonth) {
                            val (start, end) = DateUtils.getWeekDates()
                            entryRepository.getEntriesForDateRange(start, end)
                        } else {
                            // For past months, show last week of that month
                            val lastDay = selectedMonth.atEndOfMonth()
                            val startOfWeek = lastDay.minusDays(6)
                            entryRepository.getEntriesForDateRange(startOfWeek, lastDay)
                        }
                    }
                    FilterPeriod.MONTH -> {
                        entryRepository.getEntriesForMonth(selectedMonth)
                    }
                }

                val periodLabel = when (_uiState.value.selectedPeriod) {
                    FilterPeriod.DAY -> "Today"
                    FilterPeriod.WEEK -> "This Week"
                    FilterPeriod.MONTH -> DateUtils.getMonthYear(selectedMonth)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        entries = entries.sortedByDescending { entry -> entry.date },
                        periodLabel = periodLabel,
                        canGoNext = selectedMonth.isBefore(currentMonth),
                        canGoPrevious = true // Allow going back indefinitely
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

    fun selectPeriod(period: FilterPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadEntries()
    }

    fun previousMonth() {
        _uiState.update {
            it.copy(selectedMonth = it.selectedMonth.minusMonths(1))
        }
        loadEntries()
    }

    fun nextMonth() {
        val currentMonth = YearMonth.now()
        if (_uiState.value.selectedMonth.isBefore(currentMonth)) {
            _uiState.update {
                it.copy(selectedMonth = it.selectedMonth.plusMonths(1))
            }
            loadEntries()
        }
    }

    fun selectEntry(entry: DayEntry?) {
        _uiState.update { it.copy(selectedEntry = entry) }
    }

    fun dismissEntryDetails() {
        _uiState.update { it.copy(selectedEntry = null) }
    }
}
