package com.bhanu.rupeegarden.domain.usecase

import com.bhanu.rupeegarden.data.model.DayEntry
import com.bhanu.rupeegarden.data.repository.EntryRepository
import java.time.LocalDate

class StartSessionUseCase(private val entryRepository: EntryRepository) {

    suspend operator fun invoke(): Result<DayEntry> {
        return try {
            // Check if there's already an entry for today
            if (entryRepository.hasEntryForToday()) {
                return Result.failure(IllegalStateException("Already have an entry for today"))
            }

            val entry = entryRepository.createEntry(LocalDate.now())
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
