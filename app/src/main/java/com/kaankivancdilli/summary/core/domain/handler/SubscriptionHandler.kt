package com.kaankivancdilli.summary.core.domain.handler

import android.app.Activity
import com.kaankivancdilli.summary.core.billing.manager.BillingManager
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionHandler @Inject constructor(
    private val billingManager: BillingManager,
    private val subscriptionChecker: SubscriptionChecker
) {

    fun subscribeUser(
        activity: Activity,
        viewModelScope: CoroutineScope,
        pendingContent: String?,
        pendingSummarizedText: String?,
        pendingActionType: ActionType?,
        setSubscribed: (Boolean) -> Unit,
        hideDialog: () -> Unit,
        resetProcessing: () -> Unit,
        sendMessage: suspend (String, String, ActionType?) -> Unit,
        clearPending: () -> Unit
    ) {
        billingManager.startConnection {
            setupListeners(
                viewModelScope,
                pendingContent,
                pendingSummarizedText,
                pendingActionType,
                setSubscribed,
                hideDialog,
                resetProcessing,
                sendMessage,
                clearPending
            )
            billingManager.launchPurchase(activity, "monthly_subscription")
        }
    }

    private fun setupListeners(
        viewModelScope: CoroutineScope,
        pendingContent: String?,
        pendingSummarizedText: String?,
        pendingActionType: ActionType?,
        setSubscribed: (Boolean) -> Unit,
        hideDialog: () -> Unit,
        resetProcessing: () -> Unit,
        sendMessage: suspend (String, String, ActionType?) -> Unit,
        clearPending: () -> Unit
    ) {
        billingManager.setPurchaseAcknowledgedListener {
            viewModelScope.launch {
                val isNowSubscribed = subscriptionChecker.isUserSubscribed()
                setSubscribed(isNowSubscribed)

                if (isNowSubscribed && pendingContent != null && pendingSummarizedText != null && pendingActionType != null) {
                    sendMessage(pendingContent, pendingSummarizedText, pendingActionType)
                    clearPending()
                }

                hideDialog()
            }
        }

        billingManager.setSubscriptionListener { isSubscribed ->
            if (!isSubscribed) {
                resetProcessing()
                hideDialog()
            }
        }
    }
}