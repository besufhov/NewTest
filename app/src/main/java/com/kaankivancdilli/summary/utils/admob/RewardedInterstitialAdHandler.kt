package com.kaankivancdilli.summary.utils.admob

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RewardedInterstitialAdHandler(
    showAd: Boolean,
    activity: Activity,
    onUserEarnedReward: () -> Unit,
    onAdHandled: () -> Unit,
    onAdDismissed: () -> Unit // ✅ Add this parameter
) {
    if (showAd) {
        LaunchedEffect(Unit) {
            showRewardedInterstitialAd(
                activity = activity,
                onUserEarnedReward = onUserEarnedReward,
                onAdDismissed = {
                    onAdDismissed() // ✅ Call it here
                }
            )
            onAdHandled()
        }
    }
}

