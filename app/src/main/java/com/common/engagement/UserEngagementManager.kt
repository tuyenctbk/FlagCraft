package com.common.engagement

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user engagement gates, tracking first-install duration and internal counters
 * for successful user activities (such as completed flashcard rounds and trivia sessions).
 * Enforces non-intrusive modal prompt governance with a 30-day cooldown period.
 */
class UserEngagementManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "user_engagement_manager_prefs"
        const val KEY_INSTALL_TIMESTAMP = "install_timestamp"
        const val KEY_LAST_PROMPT_TIMESTAMP = "last_prompt_timestamp"
        const val KEY_FLASHCARD_ROUNDS = "flashcard_rounds_count"
        const val KEY_TRIVIA_SESSIONS = "trivia_sessions_count"
        const val KEY_SUCCESSFUL_OPERATIONS = "successful_operations_count"
        const val KEY_MANUAL_REFRESHES = "manual_refresh_count"
        const val KEY_IS_RATED = "is_rated"
        const val KEY_IS_SHARED = "is_shared"

        const val MIN_DAYS_POST_INSTALL: Long = 3L
        const val MIN_SUCCESSFUL_OPERATIONS: Int = 15
        const val COOLDOWN_DAYS: Long = 30L
        private const val DAY_IN_MS: Long = 24 * 60 * 60 * 1000L
    }

    init {
        val now = System.currentTimeMillis()
        if (!prefs.contains(KEY_INSTALL_TIMESTAMP)) {
            prefs.edit().putLong(KEY_INSTALL_TIMESTAMP, now).apply()
        }
    }

    /**
     * Increments the internal counter for completed flashcard rounds/sessions.
     */
    fun recordCompletedFlashcardRound() {
        val current = prefs.getInt(KEY_FLASHCARD_ROUNDS, 0)
        prefs.edit().putInt(KEY_FLASHCARD_ROUNDS, current + 1).apply()
        recordSuccessfulOperation()
    }

    fun recordCompletedFlashcardSession() = recordCompletedFlashcardRound()

    /**
     * Increments the internal counter for completed trivia/blitz sessions.
     */
    fun recordCompletedTriviaSession() {
        val current = prefs.getInt(KEY_TRIVIA_SESSIONS, 0)
        prefs.edit().putInt(KEY_TRIVIA_SESSIONS, current + 1).apply()
        recordSuccessfulOperation()
    }

    fun recordCompletedBlitzSession() = recordCompletedTriviaSession()

    /**
     * Increments general manual refresh / list action count.
     */
    fun recordManualRefresh() {
        val current = prefs.getInt(KEY_MANUAL_REFRESHES, 0)
        prefs.edit().putInt(KEY_MANUAL_REFRESHES, current + 1).apply()
        recordSuccessfulOperation()
    }

    /**
     * Increments general successful operation count.
     */
    fun recordSuccessfulOperation() {
        val current = prefs.getInt(KEY_SUCCESSFUL_OPERATIONS, 0)
        prefs.edit().putInt(KEY_SUCCESSFUL_OPERATIONS, current + 1).apply()
    }

    fun getFlashcardRoundsCount(): Int = prefs.getInt(KEY_FLASHCARD_ROUNDS, 0)

    fun getTriviaSessionsCount(): Int = prefs.getInt(KEY_TRIVIA_SESSIONS, 0)

    fun getSuccessfulOperationsCount(): Int = prefs.getInt(KEY_SUCCESSFUL_OPERATIONS, 0)

    fun getFirstInstallTimestamp(): Long = prefs.getLong(KEY_INSTALL_TIMESTAMP, 0L)

    fun getLastPromptTimestamp(): Long = prefs.getLong(KEY_LAST_PROMPT_TIMESTAMP, 0L)

    fun getDaysPostInstall(now: Long = System.currentTimeMillis()): Long {
        val installTime = prefs.getLong(KEY_INSTALL_TIMESTAMP, now)
        return (now - installTime) / DAY_IN_MS
    }

    /**
     * Evaluates whether an engagement dialog (Rate / Share) should be presented.
     * Enforces:
     * 1. Zero Disruption Rule: Suppress if user is currently inside an active critical flow.
     * 2. Rating & Sharing completion check: If both are already completed, suppress.
     * 3. Install duration threshold: Must be installed for at least [MIN_DAYS_POST_INSTALL] days.
     * 4. Activity threshold: Requires at least [MIN_SUCCESSFUL_OPERATIONS] completed user operations.
     * 5. Cooldown period: Minimum [COOLDOWN_DAYS] (30 days) between prompts.
     */
    fun shouldShowEngagementPrompt(
        now: Long = System.currentTimeMillis(),
        isCriticalFlowActive: Boolean = false
    ): Boolean {
        // 1. Zero Disruption Rule: Never prompt during active critical flows (gameplay, checkout, etc.)
        if (isCriticalFlowActive) return false

        // 2. Check if both rating and sharing actions are already completed
        val isRated = prefs.getBoolean(KEY_IS_RATED, false)
        val isShared = prefs.getBoolean(KEY_IS_SHARED, false)
        if (isRated && isShared) return false

        // 3. Check install duration (minimum 3 days post-install)
        if (getDaysPostInstall(now) < MIN_DAYS_POST_INSTALL) return false

        // 4. Check successful operations count (flashcards, trivia blitz sessions, manual operations)
        if (getSuccessfulOperationsCount() < MIN_SUCCESSFUL_OPERATIONS) return false

        // 5. Enforce minimum 30-day cooldown between prompts
        val lastPromptTime = prefs.getLong(KEY_LAST_PROMPT_TIMESTAMP, 0L)
        val daysSinceLastPrompt = (now - lastPromptTime) / DAY_IN_MS
        if (lastPromptTime > 0L && daysSinceLastPrompt < COOLDOWN_DAYS) return false

        return true
    }

    /**
     * Determines the specific prompt to show based on system state and engagement history.
     */
    fun evaluatePromptType(
        now: Long = System.currentTimeMillis(),
        hasPendingUpdate: Boolean = false,
        isUpdateMandatory: Boolean = false,
        isCriticalFlowActive: Boolean = false
    ): PromptType {
        // Zero Disruption Rule: Never prompt during critical flows
        if (isCriticalFlowActive) return PromptType.NONE

        // Critical updates bypass standard engagement cooldown
        if (hasPendingUpdate && isUpdateMandatory) {
            return PromptType.CRITICAL_UPDATE
        }

        // Flexible updates take priority if pending and user is not in critical flow
        if (hasPendingUpdate) {
            return PromptType.FLEXIBLE_UPDATE
        }

        // Rate / Share engagement prompts gated by install time, operations, and cooldown
        if (!shouldShowEngagementPrompt(now, isCriticalFlowActive = false)) {
            return PromptType.NONE
        }

        return when {
            !prefs.getBoolean(KEY_IS_RATED, false) -> PromptType.RATE_APP
            !prefs.getBoolean(KEY_IS_SHARED, false) -> PromptType.SHARE_APP
            else -> PromptType.NONE
        }
    }

    /**
     * Records that an engagement prompt was presented, resetting the 30-day cooldown timer.
     */
    fun recordPromptShown(now: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_PROMPT_TIMESTAMP, now).apply()
    }

    /**
     * Marks app rating as completed to prevent duplicate rating dialogs.
     */
    fun markRatingCompleted() {
        prefs.edit().putBoolean(KEY_IS_RATED, true).apply()
    }

    /**
     * Marks app sharing as completed to prevent duplicate share dialogs.
     */
    fun markShareCompleted() {
        prefs.edit().putBoolean(KEY_IS_SHARED, true).apply()
    }
}
