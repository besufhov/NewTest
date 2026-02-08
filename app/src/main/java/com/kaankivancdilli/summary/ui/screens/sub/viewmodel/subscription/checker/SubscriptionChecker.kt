package com.kaankivancdilli.summary.ui.screens.sub.viewmodel.subscription.checker

import com.kaankivancdilli.summary.utils.billing.BillingManager
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