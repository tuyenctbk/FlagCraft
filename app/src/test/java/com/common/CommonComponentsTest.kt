package com.common

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.common.engagement.EngagementTrafficController
import com.common.engagement.PromptType
import com.common.engagement.UserEngagementManager
import com.common.shake.ui.SensitivityTier
import com.common.shake.ui.classifyGForce
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CommonComponentsTest {

    @Test
    fun classifyGForce_classifiesSensitivityTiersCorrectly() {
        assertEquals(SensitivityTier.HIGH, classifyGForce(1.8f))
        assertEquals(SensitivityTier.STANDARD, classifyGForce(2.5f))
        assertEquals(SensitivityTier.LOW, classifyGForce(3.5f))
    }

    @Test
    fun engagementTrafficController_suppressesDuringCriticalFlow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = EngagementTrafficController(context)

        val prompt = controller.evaluateNextEligiblePrompt(
            hasPendingUpdate = true,
            isUpdateMandatory = true,
            isCriticalFlowActive = true
        )

        assertEquals(PromptType.NONE, prompt)
    }

    @Test
    fun engagementTrafficController_allowsMandatoryUpdateOverGlobalCooldown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val controller = EngagementTrafficController(context)

        val prompt = controller.evaluateNextEligiblePrompt(
            hasPendingUpdate = true,
            isUpdateMandatory = true,
            isCriticalFlowActive = false
        )

        assertEquals(PromptType.CRITICAL_UPDATE, prompt)
    }

    @Test
    fun userEngagementManager_tracksInternalCountersAndInstallTimestamp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = UserEngagementManager(context)

        // Verifies first-install timestamp is persisted
        assertTrue(manager.getFirstInstallTimestamp() > 0L)

        // Tracks flashcard and trivia sessions independently
        repeat(4) { manager.recordCompletedFlashcardRound() }
        repeat(3) { manager.recordCompletedTriviaSession() }

        assertEquals(4, manager.getFlashcardRoundsCount())
        assertEquals(3, manager.getTriviaSessionsCount())
        assertEquals(7, manager.getSuccessfulOperationsCount())
    }

    @Test
    fun userEngagementManager_requiresMinimumInstallDurationAndOperations() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = UserEngagementManager(context)
        val now = System.currentTimeMillis()

        // 1. Immediately after install (0 days, 0 operations): should not show
        assertFalse(manager.shouldShowEngagementPrompt(now = now))

        // 2. Perform operations through domain methods (flashcards & trivia), but still day 0: should not show
        repeat(8) { manager.recordCompletedFlashcardRound() }
        repeat(7) { manager.recordCompletedTriviaSession() }
        assertEquals(15, manager.getSuccessfulOperationsCount())
        assertFalse(manager.shouldShowEngagementPrompt(now = now))

        // 3. Fast forward 4 days (3+ post-install): should show prompt
        val fourDaysLater = now + (4L * 24 * 60 * 60 * 1000L)
        assertTrue(manager.shouldShowEngagementPrompt(now = fourDaysLater))

        // 4. Record prompt shown, verify 30-day cooldown suppresses prompt 10 days later
        manager.recordPromptShown(now = fourDaysLater)
        val fourteenDaysLater = now + (14L * 24 * 60 * 60 * 1000L)
        assertFalse(manager.shouldShowEngagementPrompt(now = fourteenDaysLater))

        // 5. Fast forward 35 days post-prompt: should be eligible again
        val thirtyNineDaysLater = now + (39L * 24 * 60 * 60 * 1000L)
        assertTrue(manager.shouldShowEngagementPrompt(now = thirtyNineDaysLater))
    }

    @Test
    fun userEngagementManager_evaluatesPromptHierarchyCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = UserEngagementManager(context)
        val now = System.currentTimeMillis()
        val eligibleTime = now + (5L * 24 * 60 * 60 * 1000L)

        // Give sufficient operations (15+)
        repeat(15) { manager.recordCompletedBlitzSession() }

        // When critical flow is active, suppress normal prompts
        assertEquals(
            PromptType.NONE,
            manager.evaluatePromptType(now = eligibleTime, isCriticalFlowActive = true)
        )

        // Mandatory update bypasses even if engagement gates aren't checked, but still respects critical flow
        assertEquals(
            PromptType.CRITICAL_UPDATE,
            manager.evaluatePromptType(
                now = eligibleTime,
                hasPendingUpdate = true,
                isUpdateMandatory = true,
                isCriticalFlowActive = false
            )
        )

        // Flexible update takes priority when update is pending
        assertEquals(
            PromptType.FLEXIBLE_UPDATE,
            manager.evaluatePromptType(
                now = eligibleTime,
                hasPendingUpdate = true,
                isUpdateMandatory = false,
                isCriticalFlowActive = false
            )
        )

        // First prompt eligible is Rate App
        assertEquals(
            PromptType.RATE_APP,
            manager.evaluatePromptType(now = eligibleTime, hasPendingUpdate = false)
        )

        // Once rated, should prioritize Share App
        manager.markRatingCompleted()
        assertEquals(
            PromptType.SHARE_APP,
            manager.evaluatePromptType(now = eligibleTime, hasPendingUpdate = false)
        )

        // Once shared, nothing further
        manager.markShareCompleted()
        assertEquals(
            PromptType.NONE,
            manager.evaluatePromptType(now = eligibleTime, hasPendingUpdate = false)
        )
    }

    @Test
    fun appSpecificEngagementManagerAlias_functionsCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appManager = com.example.data.engagement.UserEngagementManager(context)
        assertTrue(appManager.getFirstInstallTimestamp() > 0L)
    }
}
