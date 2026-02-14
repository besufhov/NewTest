package com.kaankivancdilli.summary.ui.viewmodel.sub.subscription

import androidx.lifecycle.ViewModel
import com.kaankivancdilli.summary.core.billing.manager.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    init {
        checkSubscription()
        billingManager.setSubscriptionListener { isSubscribed ->
            _isSubscribed.value = isSubscribed // Update live subscription status
        }
    }

    fun checkSubscription() {
        billingManager.isUserSubscribed { isSubscribed ->
            _isSubscribed.value = isSubscribed
        }
    }
}