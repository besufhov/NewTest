package com.kaankivancdilli.summary.utils.rate

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import android.content.Intent
import android.net.Uri

fun requestInAppReview(activity: Activity) {
    val manager = ReviewManagerFactory.create(activity)
    val request = manager.requestReviewFlow()

    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener {
                // ✅ Flow finished (you don’t know if rating was submitted)
            }
        } else {
            // ❌ If failed, fallback (optional)
            openPlayStorePage(activity)
        }
    }
}

fun openPlayStorePage(activity: Activity) {
    val packageName = activity.packageName
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName")
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.startActivity(intent)
}