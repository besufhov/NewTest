package com.kaankivancdilli.summary.ui.component.admob.ad

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

fun showRewardedInterstitialAd(
    activity: Activity,
    onUserEarnedReward: () -> Unit,
    onAdDismissed: () -> Unit
) {
    val adRequest = AdRequest.Builder().build()

    RewardedInterstitialAd.load(
        activity,
        "ca-app-pub-7431967243613151/6104732513", // ✅ Your real rewarded interstitial ad unit ID
      //  "ca-app-pub-3940256099942544/5354046379", // Test
        adRequest,
        object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("Ad", "Rewarded interstitial dismissed")
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        Log.d("Ad", "Failed to show rewarded interstitial: ${error.message}")
                        onAdDismissed()
                    }
                }

                ad.show(activity) { rewardItem ->
                    Log.d("Ad", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    onUserEarnedReward()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.d("Ad", "Rewarded interstitial failed to load: ${error.message}")
                onAdDismissed()
            }
        }
    )
}