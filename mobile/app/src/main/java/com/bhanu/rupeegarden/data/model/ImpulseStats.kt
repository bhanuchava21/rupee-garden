package com.bhanu.rupeegarden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImpulseStats(
    val totalImpulsesResisted: Int = 0,
    val totalImpulsesBought: Int = 0,
    val totalMoneySavedByResisting: Double = 0.0
) {
    val totalChecks: Int get() = totalImpulsesResisted + totalImpulsesBought

    val successRate: Float get() = if (totalChecks > 0)
        totalImpulsesResisted.toFloat() / totalChecks else 0f
}
