package com.kaankivancdilli.summary.utils.admob.adhandler

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.kaankivancdilli.summary.utils.admob.ad.showInterstitialAd

@Composable
fun InterstitialAdHandler(
    showAd: Boolean,
    activity: Activity,
    onAdDismissed: () -> Unit,
    onAdHandled: () -> Unit
) {
    if (showAd) {
        LaunchedEffect(Unit) {
            showInterstitialAd(activity) {
                onAdDismissed()
            }
            onAdHandled()
        }
    }
}

