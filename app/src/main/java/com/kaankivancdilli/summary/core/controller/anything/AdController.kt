package com.kaankivancdilli.summary.core.controller.anything

import com.kaankivancdilli.summary.core.state.FreeUsageTracker
import javax.inject.Inject

class AdController @Inject constructor(
    private val freeUsageTracker: FreeUsageTracker
) {
    private var skipInterstitialOnceAfterReset = false

    fun shouldSkipInterstitial(): Boolean {
        if (skipInterstitialOnceAfterReset) {
            skipInterstitialOnceAfterReset = false
            return true
        }
        return false
    }

    suspend fun onRewardEarned() {
        freeUsageTracker.resetCount()
        skipInterstitialOnceAfterReset = true
    }
}