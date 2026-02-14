package com.kaankivancdilli.summary.core.review

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.kaankivancdilli.summary.core.review.play.openPlayStorePage

fun requestInAppReview(activity: Activity) {
    val manager = ReviewManagerFactory.create(activity)
    val request = manager.requestReviewFlow()

    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener {
            }
        } else {
            openPlayStorePage(activity)
        }
    }
}