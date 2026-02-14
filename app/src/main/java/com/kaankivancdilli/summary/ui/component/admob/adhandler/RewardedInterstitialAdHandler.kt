package com.kaankivancdilli.summary.ui.component.admob.adhandler

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.kaankivancdilli.summary.ui.component.admob.ad.showRewardedInterstitialAd

@Composable
fun RewardedInterstitialAdHandler(
    showAd: Boolean,
    activity: Activity,
    onUserEarnedReward: () -> Unit,
    onAdHandled: () -> Unit,
    onAdDismissed: () -> Unit
) {
    if (showAd) {
        LaunchedEffect(Unit) {
            showRewardedInterstitialAd(
                activity = activity,
                onUserEarnedReward = onUserEarnedReward,
                onAdDismissed = {
                    onAdDismissed()
                }
            )
            onAdHandled()
        }
    }
}