package com.bhanu.rupeegarden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DayEntry(
    val id: String,
    val date: String, // ISO format: "2024-01-15"
    val startedAt: Long, // Timestamp when session started
    val completedAt: Long? = null, // Timestamp when session completed
    val saved: Boolean? = null, // null = in progress, true = saved, false = spent
    val spentAmount: Double? = null,
    val spentCategory: SpendingCategory? = null,
    val spentDescription: String? = null,
    val xpEarned: Int = 0
) {
    val isCompleted: Boolean get() = completedAt != null
    val isInProgress: Boolean get() = completedAt == null
}
