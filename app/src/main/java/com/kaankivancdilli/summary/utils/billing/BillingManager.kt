package com.kaankivancdilli.summary.utils.billing

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.kaankivancdilli.summary.utils.reusable.BillingManagerHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

class BillingManager @Inject constructor(private val application: Application) :
    PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(application)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()


    private var onSubscriptionUpdated: ((Boolean) -> Unit)? = null
    private var onPurchaseAcknowledged: (() -> Unit)? = null
    private var onPurchaseFlowClosed: (() -> Unit)? = null

    fun setSubscriptionListener(listener: (Boolean) -> Unit) {
        onSubscriptionUpdated = listener
    }

    fun setPurchaseAcknowledgedListener(listener: () -> Unit) {
        onPurchaseAcknowledged = listener
    }

    fun setPurchaseFlowClosedListener(listener: () -> Unit) {
        onPurchaseFlowClosed = listener
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        } else {
                            onPurchaseAcknowledged?.invoke()
                        }
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d("Billing", "User canceled purchase")
                onSubscriptionUpdated?.invoke(false) // ⬅️ stops spinner
                onPurchaseFlowClosed?.invoke()
            }

            else -> {
                Log.e("Billing", "Purchase failed: ${billingResult.debugMessage}")
                onSubscriptionUpdated?.invoke(false)
                onPurchaseFlowClosed?.invoke()
            }
        }
    }

    fun startConnection(onConnected: () -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    onConnected()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("Billing", "Service disconnected")
                onSubscriptionUpdated?.invoke(false)
                onPurchaseFlowClosed?.invoke()
            }
        })
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { result, detailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && detailsList.productDetailsList.isNotEmpty()) {
                val productDetails = detailsList.productDetailsList.first()
                val offer = productDetails.subscriptionOfferDetails?.firstOrNull()
                if (offer != null) {
                    val params = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offer.offerToken)
                                    .build()
                            )
                        )
                        .build()

                    // 🚫 Don't launch a coroutine or cancel early
                    billingClient.launchBillingFlow(activity, params)
                } else {
                    Toast.makeText(application, "No valid offer found", Toast.LENGTH_SHORT).show()
                    onSubscriptionUpdated?.invoke(false)
                    onPurchaseFlowClosed?.invoke()
                }
            } else {
                Toast.makeText(application, "Subscription not available", Toast.LENGTH_SHORT).show()
                onSubscriptionUpdated?.invoke(false)
                onPurchaseFlowClosed?.invoke()
            }
        }
    }


    private fun acknowledgePurchase(purchase: Purchase, retryCount: Int = 0) {
        if (purchase.isAcknowledged) {
            onPurchaseAcknowledged?.invoke()
            return
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                isUserSubscribed { onSubscriptionUpdated?.invoke(it) }
                onPurchaseAcknowledged?.invoke()
                BillingManagerHolder.notifyAcknowledged()
            } else {
                if (retryCount < 3) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        acknowledgePurchase(purchase, retryCount + 1)
                    }, 2000L * (2.0.pow(retryCount.toDouble())).toLong())
                } else {
                    isUserSubscribed { onSubscriptionUpdated?.invoke(it) }
                    onPurchaseAcknowledged?.invoke()
                }
            }
        }
    }

    fun isUserSubscribed(callback: (Boolean) -> Unit) {
        if (!billingClient.isReady) {
            startConnection {
                isUserSubscribed(callback)
            }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            val isSubscribed = result.responseCode == BillingClient.BillingResponseCode.OK &&
                    purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED && it.isAcknowledged }

            callback(isSubscribed)
        }
    }
}


