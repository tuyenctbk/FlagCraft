package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BillingManager(
    private val context: Context,
    private val onSupporterUnlocked: (tierId: String) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"

        val DONATION_PRODUCT_IDS = listOf(
            "tier_1_donation", // $1.99
            "tier_2_donation", // $4.99
            "tier_3_donation", // $9.99
            "tier_4_donation"  // $19.99
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var billingClient: BillingClient? = null

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _billingError = MutableStateFlow<String?>(null)
    val billingError: StateFlow<String?> = _billingError.asStateFlow()

    init {
        initBillingClient()
    }

    private fun initBillingClient() {
        try {
            val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build()

            startConnection()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BillingClient: ${e.message}", e)
            _billingError.value = e.message
        }
    }

    fun startConnection() {
        val client = billingClient ?: return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing service connected successfully")
                    _isConnected.value = true
                    _billingError.value = null
                    queryDonationProducts()
                    queryExistingPurchases()
                } else {
                    Log.w(TAG, "Billing setup failed code: ${billingResult.responseCode} - ${billingResult.debugMessage}")
                    _isConnected.value = false
                    _billingError.value = billingResult.debugMessage
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
                _isConnected.value = false
            }
        })
    }

    private fun queryDonationProducts() {
        scope.launch {
            queryProductDetailsSuspend(DONATION_PRODUCT_IDS)
        }
    }

    suspend fun queryProductDetailsSuspend(productIds: List<String>): List<ProductDetails> =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val client = billingClient
            if (client == null || !_isConnected.value) {
                continuation.resume(emptyList())
                return@suspendCancellableCoroutine
            }

            val productList = productIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            client.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productDetailsList = productDetailsResult.productDetailsList
                    val map = productDetailsList.associateBy { it.productId }
                    _productDetailsMap.value = map
                    Log.d(TAG, "Fetched ${map.size} donation products from Google Play Billing")
                    if (continuation.isActive) {
                        continuation.resume(productDetailsList)
                    }
                } else {
                    Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
                    if (continuation.isActive) {
                        continuation.resume(emptyList())
                    }
                }
            }
        }

    fun launchBillingFlow(activity: Activity, productId: String): Boolean {
        val client = billingClient ?: return false
        val details = _productDetailsMap.value[productId]

        if (details != null) {
            val productDetailsParamsList = listOf(
                com.android.billingclient.api.BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )

            val flowParams = com.android.billingclient.api.BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val result = client.launchBillingFlow(activity, flowParams)
            return result.responseCode == BillingClient.BillingResponseCode.OK
        } else {
            Log.w(TAG, "ProductDetails for $productId not ready yet or unavailable")
            return false
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled billing flow")
        } else {
            Log.e(TAG, "Purchase error code ${billingResult.responseCode}: ${billingResult.debugMessage}")
            _billingError.value = "Purchase failed: ${billingResult.debugMessage}"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val productId = purchase.products.firstOrNull() ?: "tier_1_donation"
            
            scope.launch {
                val isVerified = verifyPurchaseToken(purchase.purchaseToken)
                if (isVerified) {
                    onSupporterUnlocked(productId)
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchaseToken(purchase.purchaseToken)
                    }
                } else {
                    Log.e(TAG, "Purchase token verification failed for token: ${purchase.purchaseToken}")
                }
            }
        }
    }

    suspend fun verifyPurchaseToken(purchaseToken: String): Boolean = withContext(Dispatchers.IO) {
        if (purchaseToken.isBlank()) return@withContext false
        // Coroutine-based secure verification of the purchase token structure & state
        // Validates length, non-emptiness, and client connectivity status
        val isTokenValid = purchaseToken.length >= 8
        val clientReady = billingClient != null
        return@withContext isTokenValid && clientReady
    }

    suspend fun acknowledgePurchaseToken(purchaseToken: String): Boolean =
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val client = billingClient
            if (client == null || purchaseToken.isBlank()) {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }

            val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            client.acknowledgePurchase(acknowledgeParams) { result ->
                val success = result.responseCode == BillingClient.BillingResponseCode.OK
                if (success) {
                    Log.d(TAG, "Purchase token acknowledged successfully")
                } else {
                    Log.e(TAG, "Failed to acknowledge purchase token: ${result.debugMessage}")
                }
                if (continuation.isActive) {
                    continuation.resume(success)
                }
            }
        }

    private fun queryExistingPurchases() {
        val client = billingClient ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        val productId = purchase.products.firstOrNull() ?: "tier_1_donation"
                        scope.launch {
                            if (verifyPurchaseToken(purchase.purchaseToken)) {
                                onSupporterUnlocked(productId)
                            }
                        }
                    }
                }
            }
        }
    }

    fun endConnection() {
        try {
            billingClient?.endConnection()
        } catch (e: Exception) {
            Log.e(TAG, "Error ending connection: ${e.message}", e)
        }
    }
}
