package com.kaankivancdilli.summary.ui.screens.sub.viewmodel.subscription.viewmodel

import androidx.lifecycle.ViewModel
import com.kaankivancdilli.summary.utils.billing.BillingManager
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
        checkSubscription() // Check on init
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

