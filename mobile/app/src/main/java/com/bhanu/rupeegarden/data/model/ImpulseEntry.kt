package com.bhanu.rupeegarden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImpulseEntry(
    val id: String,
    val itemName: String,
    val amount: Double,
    val category: SpendingCategory? = null,
    val timestamp: Long,
    val isEssential: Boolean,
    val ownsSimilar: Boolean,
    val currentBroken: Boolean,
    val canWait: Boolean,
    val impulseScore: Int,
    val verdict: ImpulseVerdict,
    val result: ImpulseResult,
    val xpEarned: Int = 0
)

@Serializable
enum class ImpulseVerdict(val displayMessage: String) {
    GO_AHEAD("Valid purchase, go ahead!"),
    MAYBE_WAIT("Consider waiting a few days"),
    STRONG_NO("Strong impulse! You don't need this")
}

@Serializable
enum class ImpulseResult {
    RESISTED,
    BOUGHT,
    ABANDONED
}
