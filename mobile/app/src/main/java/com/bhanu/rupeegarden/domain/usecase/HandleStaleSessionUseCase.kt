package com.bhanu.rupeegarden.domain.usecase

import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.repository.EntryRepository
import com.bhanu.rupeegarden.data.repository.ProgressRepository
import com.bhanu.rupeegarden.util.XpCalculator
import java.time.LocalDate

class HandleStaleSessionUseCase(
    private val entryRepository: EntryRepository,
    private val progressRepository: ProgressRepository
) {

    suspend operator fun invoke(): StaleSessionResult {
        val activeSession = entryRepository.getActiveSession() ?: return StaleSessionResult.NoSession

        val sessionDate = LocalDate.parse(activeSession.date)
        val today = LocalDate.now()

        return if (sessionDate.isBefore(today)) {
            // Session is from a previous day - auto-complete as "spent"
            val completionXp = XpCalculator.SPENT_DAY_XP

            val completedEntry = entryRepository.completeEntry(
                entry = activeSession,
                saved = false,
                additionalXp = completionXp
            )

            progressRepository.addXp(
                xp = completionXp,
                date = sessionDate,
                saved = false
            )

            StaleSessionResult.AutoCompleted(completedEntry)
        } else {
            // Session is from today - still active
            StaleSessionResult.ActiveSession(activeSession)
        }
    }
}

sealed class StaleSessionResult {
    object NoSession : StaleSessionResult()
    data class ActiveSession(val entry: DayEntry) : StaleSessionResult()
    data class AutoCompleted(val entry: DayEntry) : StaleSessionResult()
}
