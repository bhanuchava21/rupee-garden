package com.bhanu.rupeegarden.util

import com.bhanu.rupeegarden.data.model.ImpulseVerdict

object ImpulseScoreCalculator {

    private const val BASE_SCORE = 5

    /**
     * Calculates the impulse score based on the user's answers.
     *
     * Score impacts:
     * - Essential? Yes: -3 (valid need), No: +3 (luxury)
     * - Own similar? Yes: +2 (less needed), No: -2 (don't have)
     * - Current broken? Yes: -2 (replacement), No: +2 (still works)
     * - Can wait? Yes: +2 (not urgent), No: -1 (urgent)
     *
     * @return Score from 1-10, higher means should resist more
     */
    fun calculateScore(
        isEssential: Boolean,
        ownsSimilar: Boolean,
        currentBroken: Boolean,
        canWait: Boolean
    ): Int {
        var score = BASE_SCORE

        // Essential or Luxury?
        score += if (isEssential) -3 else 3

        // Own something similar?
        score += if (ownsSimilar) 2 else -2

        // Current one broken?
        score += if (currentBroken) -2 else 2

        // Can this wait a week?
        score += if (canWait) 2 else -1

        return score.coerceIn(1, 10)
    }

    /**
     * Returns the verdict based on the impulse score.
     *
     * Score 1-3: GO_AHEAD - Valid purchase
     * Score 4-6: MAYBE_WAIT - Consider waiting
     * Score 7-10: STRONG_NO - Strong impulse, resist
     */
    fun getVerdict(score: Int): ImpulseVerdict {
        return when {
            score <= 3 -> ImpulseVerdict.GO_AHEAD
            score <= 6 -> ImpulseVerdict.MAYBE_WAIT
            else -> ImpulseVerdict.STRONG_NO
        }
    }

    /**
     * Returns a motivational message based on the verdict.
     */
    fun getMotivationalMessage(verdict: ImpulseVerdict): String {
        return when (verdict) {
            ImpulseVerdict.GO_AHEAD -> "This seems like a reasonable purchase. If you've budgeted for it, go ahead!"
            ImpulseVerdict.MAYBE_WAIT -> "Take a moment. Sleep on it. If you still want it tomorrow, it might be worth it."
            ImpulseVerdict.STRONG_NO -> "Your future self will thank you for waiting. This is peak impulse territory!"
        }
    }
}
