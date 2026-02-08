package com.kaankivancdilli.summary.utils.admob

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

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

