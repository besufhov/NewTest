package com.kaankivancdilli.summary.utils.documents.share.text

import android.content.Context
import android.content.Intent
import com.kaankivancdilli.summary.R

fun sharePlainText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = (Intent.createChooser(sendIntent, context.getString(R.string.share_as_text)))

    context.startActivity(shareIntent)
}