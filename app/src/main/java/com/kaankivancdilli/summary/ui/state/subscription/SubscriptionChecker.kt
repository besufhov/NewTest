package com.kaankivancdilli.summary.ui.state.subscription

import com.kaankivancdilli.summary.core.billing.manager.BillingManager
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject


class SubscriptionChecker @Inject constructor(
    private val billingManager: BillingManager
) {
    suspend fun isUserSubscribed(): Boolean = suspendCancellableCoroutine { cont ->
        billingManager.isUserSubscribed {
            cont.resume(it) {}
        }
    }
}