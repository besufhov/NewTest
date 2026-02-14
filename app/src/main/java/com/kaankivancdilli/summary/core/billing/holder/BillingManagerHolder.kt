package com.kaankivancdilli.summary.core.billing.holder

object BillingManagerHolder {

    private var onAcknowledged: (() -> Unit)? = null
    private var onCancelled: (() -> Unit)? = null

    fun setOnAcknowledged(callback: () -> Unit) {
        onAcknowledged = callback
    }

    fun notifyAcknowledged() {
        onAcknowledged?.invoke()
    }

    fun setOnPurchaseCancelled(callback: () -> Unit) {
        onCancelled = callback
    }

    fun notifyPurchaseCancelled() {
        onCancelled?.invoke()
    }
}