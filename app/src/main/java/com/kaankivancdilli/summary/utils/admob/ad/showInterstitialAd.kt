package com.kaankivancdilli.summary.utils.admob.ad

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
        activity,
        "ca-app-pub-7431967243613151/7711627538", // Your real interstitial ad unit ID
       // "ca-app-pub-3940256099942544/1033173712", // Test
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        onAdDismissed()
                    }
                }
                ad.show(activity)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("Ad", "Interstitial ad failed to load: ${adError.message}")
                onAdDismissed()
            }
        }
    )
}
