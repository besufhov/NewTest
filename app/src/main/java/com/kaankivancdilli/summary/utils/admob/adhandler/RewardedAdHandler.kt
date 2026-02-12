package com.kaankivancdilli.summary.utils.admob.adhandler

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.kaankivancdilli.summary.utils.admob.ad.showRewardedAd

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


