package com.bhanu.rupeegarden.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class SpendingCategory(val displayName: String, val emoji: String) {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    ENTERTAINMENT("Entertainment", "🎬"),
    BILLS("Bills & Utilities", "📱"),
    HEALTH("Health", "💊"),
    OTHER("Other", "📦")
}
