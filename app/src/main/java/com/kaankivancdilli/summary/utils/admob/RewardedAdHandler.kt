package com.kaankivancdilli.summary.utils.admob

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RewardedAdHandler(
    showAd: Boolean,
    activity: Activity,
    onUserEarnedReward: () -> Unit,
    onAdHandled: () -> Unit
) {
    if (showAd) {
        LaunchedEffect(Unit) {
            showRewardedAd(activity) {
                onUserEarnedReward()
            }
            onAdHandled()
        }
    }
}


