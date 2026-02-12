package com.kaankivancdilli.summary.ui.viewmodel.main.settings

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaankivancdilli.summary.utils.state.usage.FreeUsageTracker
import com.kaankivancdilli.summary.utils.billing.BillingManager
import com.kaankivancdilli.summary.ui.viewmodel.sub.subscription.SubscriptionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

   // private val billingManager = BillingManager(application)

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

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    fun toggleDarkMode() {
        _darkMode.value = !_darkMode.value
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun subscribe(activity: Activity, subscriptionViewModel: SubscriptionViewModel) {
        billingManager.startConnection {
            billingManager.launchPurchase(activity, "monthly_subscription")
            subscriptionViewModel.checkSubscription() // Ensure UI updates
        }
    }

    fun resetFreeUsageCount() {
        viewModelScope.launch {
            freeUsageTracker.resetCount()
            Log.d("SettingsViewModel", "Usage count reset after reward")
        }
    }
}

