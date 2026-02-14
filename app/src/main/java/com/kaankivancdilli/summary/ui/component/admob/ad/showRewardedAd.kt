package com.kaankivancdilli.summary.ui.component.admob.ad

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

fun showRewardedAd(activity: Activity, onUserEarnedReward: () -> Unit) {
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(
        activity,
        "ca-app-pub-7431967243613151/9613852245",
       // "ca-app-pub-3940256099942544/5224354917", // test
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("Ad", "Rewarded ad dismissed")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.d("Ad", "Rewarded ad failed to show: ${adError.message}")
                    }
                }

                rewardedAd.show(activity) { rewardItem ->
                    // ✅ The user watched the ad — give the reward
                    Log.d("Ad", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    onUserEarnedReward()
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.d("Ad", "Rewarded ad failed to load: ${loadAdError.message}")
            }
        }
    )
}