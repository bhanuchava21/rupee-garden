package com.bhanu.rupeegarden.util

object XpCalculator {
    const val START_SESSION_XP = 5
    const val SAVED_DAY_XP = 50
    const val SPENT_DAY_XP = 10
    const val XP_PER_LEVEL = 200
    const val IMPULSE_COMPLETION_XP = 5
    const val IMPULSE_RESISTED_XP = 25

    fun calculateLevel(totalXp: Int): Int {
        return (totalXp / XP_PER_LEVEL) + 1
    }

    fun calculateXpInCurrentLevel(totalXp: Int): Int {
        return totalXp % XP_PER_LEVEL
    }

    fun calculateXpToNextLevel(totalXp: Int): Int {
        return XP_PER_LEVEL - calculateXpInCurrentLevel(totalXp)
    }

    fun getCompletionXp(saved: Boolean): Int {
        return if (saved) SAVED_DAY_XP else SPENT_DAY_XP
    }

    fun getTotalXpForDay(saved: Boolean): Int {
        return START_SESSION_XP + getCompletionXp(saved)
    }

    fun getImpulseXp(resisted: Boolean): Int {
        return if (resisted) {
            IMPULSE_COMPLETION_XP + IMPULSE_RESISTED_XP
        } else {
            IMPULSE_COMPLETION_XP
        }
    }
}
