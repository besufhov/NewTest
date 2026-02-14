package com.kaankivancdilli.summary.core.review.play

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun openPlayStorePage(activity: Activity) {
    val packageName = activity.packageName
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName")
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.startActivity(intent)
}