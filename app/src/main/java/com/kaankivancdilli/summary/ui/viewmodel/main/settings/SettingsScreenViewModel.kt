package com.kaankivancdilli.summary.ui.viewmodel.main.settings

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.core.state.FreeUsageTracker
import com.kaankivancdilli.summary.core.billing.manager.BillingManager
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val freeUsageTracker: FreeUsageTracker
) : ViewModel() {

    val usageCount: StateFlow<Int> = freeUsageTracker.countFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalUsageCount: StateFlow<Int> = freeUsageTracker.totalCountFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun subscribe(activity: Activity, subscriptionViewModel: SubscriptionViewModel) {
        billingManager.startConnection {
            billingManager.launchPurchase(activity, "monthly_subscription")
            subscriptionViewModel.checkSubscription()
        }
    }

    fun resetFreeUsageCount() {
        viewModelScope.launch {
            freeUsageTracker.resetCount()
            Log.d("SettingsViewModel", "Usage count reset after reward")
        }
    }
}