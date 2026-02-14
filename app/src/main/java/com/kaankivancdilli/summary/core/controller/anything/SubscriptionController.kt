package com.kaankivancdilli.summary.core.controller.anything

import android.app.Activity
import com.kaankivancdilli.summary.core.billing.manager.BillingManager
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionController @Inject constructor(
    private val subscriptionChecker: SubscriptionChecker,
    private val billingManager: BillingManager
) {

    fun subscribe(
        activity: Activity,
        scope: CoroutineScope,
        onSubscribed: () -> Unit,
        onDialogDismiss: () -> Unit
    ) {
        billingManager.startConnection {
            billingManager.setPurchaseAcknowledgedListener {
                scope.launch {
                    if (subscriptionChecker.isUserSubscribed()) {
                        onSubscribed()
                    }
                    onDialogDismiss()
                }
            }

            billingManager.launchPurchase(activity, "monthly_subscription")
        }
    }
}