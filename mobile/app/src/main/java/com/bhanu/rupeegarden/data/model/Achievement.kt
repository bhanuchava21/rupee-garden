package com.bhanu.rupeegarden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlockedAt: Long? = null // null means locked
) {
    val isUnlocked: Boolean get() = unlockedAt != null
}

enum class AchievementType(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String
) {
    // Streak achievements
    FIRST_SAVE("first_save", "First Save", "Complete your first save day", "🌱"),
    WEEK_WARRIOR("week_warrior", "Week Warrior", "Achieve a 7-day streak", "🔥"),
    MONTH_MASTER("month_master", "Month Master", "Achieve a 30-day streak", "⭐"),
    CENTURY_SAVER("century_saver", "Century Saver", "Achieve a 100-day streak", "💯"),

    // Level achievements
    LEVEL_5("level_5", "Rising Star", "Reach Level 5", "⬆️"),
    LEVEL_10("level_10", "Double Digits", "Reach Level 10", "🔟"),
    LEVEL_25("level_25", "Quarter Century", "Reach Level 25", "🏅"),
    LEVEL_50("level_50", "Half Century", "Reach Level 50", "🏆"),

    // Total saves achievements
    SAVED_10("saved_10", "Getting Started", "Save for 10 days total", "🌿"),
    SAVED_50("saved_50", "Dedicated Saver", "Save for 50 days total", "🌳"),
    SAVED_100("saved_100", "Savings Champion", "Save for 100 days total", "🏰"),

    // Garden achievements
    FIRST_TREE("first_tree", "First Tree", "Plant your first tree", "🌲"),
    FULL_GARDEN("full_garden", "Full Garden", "Have 16+ trees in a month", "🏡"),

    // XP achievements
    XP_1000("xp_1000", "XP Hunter", "Earn 1,000 XP total", "✨"),
    XP_5000("xp_5000", "XP Master", "Earn 5,000 XP total", "💫"),
    XP_10000("xp_10000", "XP Legend", "Earn 10,000 XP total", "🌟");

    fun toAchievement(unlockedAt: Long? = null) = Achievement(
        id = id,
        title = title,
        description = description,
        emoji = emoji,
        unlockedAt = unlockedAt
    )
}
