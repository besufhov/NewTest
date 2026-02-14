package com.kaankivancdilli.summary.utils.documents.share.docx

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.kaankivancdilli.summary.R
import java.io.File

fun shareDocxFromDownloads(context: Context, fileName: String) {
    val fileNameWithExt = if (fileName.endsWith(".docx")) fileName else "$fileName.docx"

    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileNameWithExt)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_docx)))
}