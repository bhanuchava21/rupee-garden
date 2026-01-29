package com.bhanu.rupeegarden.data.repository

import com.bhanu.rupeegarden.data.datastore.RupeeGardenDataStore
import com.bhanu.rupeegarden.data.model.ImpulseEntry
import com.bhanu.rupeegarden.data.model.ImpulseResult
import com.bhanu.rupeegarden.data.model.ImpulseStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ImpulseRepository(private val dataStore: RupeeGardenDataStore) {

    val impulseEntries: Flow<List<ImpulseEntry>> = dataStore.impulseEntries
    val impulseStats: Flow<ImpulseStats> = dataStore.impulseStats

    suspend fun getImpulseStats(): ImpulseStats {
        return dataStore.impulseStats.first()
    }

    suspend fun addImpulseEntry(entry: ImpulseEntry): ImpulseStats {
        val currentEntries = dataStore.impulseEntries.first()
        val updatedEntries = currentEntries + entry
        dataStore.saveImpulseEntries(updatedEntries)

        // Update stats
        val currentStats = dataStore.impulseStats.first()
        val updatedStats = when (entry.result) {
            ImpulseResult.RESISTED -> currentStats.copy(
                totalImpulsesResisted = currentStats.totalImpulsesResisted + 1,
                totalMoneySavedByResisting = currentStats.totalMoneySavedByResisting + entry.amount
            )
            ImpulseResult.BOUGHT -> currentStats.copy(
                totalImpulsesBought = currentStats.totalImpulsesBought + 1
            )
            ImpulseResult.ABANDONED -> currentStats // No stats change for abandoned
        }
        dataStore.saveImpulseStats(updatedStats)
        return updatedStats
    }

    suspend fun getRecentEntries(limit: Int = 10): List<ImpulseEntry> {
        return dataStore.impulseEntries.first()
            .sortedByDescending { it.timestamp }
            .take(limit)
    }
}
