package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.billing.BillingManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BillingManagerTest {

    private lateinit var context: Context
    private lateinit var billingManager: BillingManager
    private var supporterUnlockedTier: String? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        billingManager = BillingManager(context) { tierId ->
            supporterUnlockedTier = tierId
        }
    }

    @Test
    fun donationProductIds_containsExpectedTiers() {
        val tiers = BillingManager.DONATION_PRODUCT_IDS
        assertEquals(4, tiers.size)
        assertTrue(tiers.contains("tier_1_donation"))
        assertTrue(tiers.contains("tier_2_donation"))
        assertTrue(tiers.contains("tier_3_donation"))
        assertTrue(tiers.contains("tier_4_donation"))
    }

    @Test
    fun verifyPurchaseToken_rejectsBlankToken() = runBlocking {
        val isValid = billingManager.verifyPurchaseToken("")
        assertFalse(isValid)
    }

    @Test
    fun verifyPurchaseToken_acceptsValidToken() = runBlocking {
        val isValid = billingManager.verifyPurchaseToken("sample_valid_purchase_token_12345")
        assertTrue(isValid)
    }

    @Test
    fun queryProductDetailsSuspend_returnsListOnMockContext() = runBlocking {
        val details = billingManager.queryProductDetailsSuspend(BillingManager.DONATION_PRODUCT_IDS)
        assertNotNull(details)
    }
}
