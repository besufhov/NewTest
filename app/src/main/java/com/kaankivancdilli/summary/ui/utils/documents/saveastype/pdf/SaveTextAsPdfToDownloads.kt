package com.kaankivancdilli.summary.ui.utils.documents.saveastype.pdf

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.kaankivancdilli.summary.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun saveTextAsPdfToDownloads(context: Context, fileName: String, text: String) {
    val fileNameWithExt = "$fileName.pdf"

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    val activity = context.findActivity()
    if (Build.VERSION.SDK_INT in 23..28 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
    ) {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1002
            )
        } ?: run {
            Toast.makeText(context, "Permission request requires an Activity", Toast.LENGTH_SHORT).show()
        }
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Scoped storage (Android 10+)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameWithExt)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")?.let {
            resolver.insert(it, contentValues)
        }

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                generatePdf(text, outputStream)
            }
            Toast.makeText(context, context.getString(R.string.saved_as_pdf), Toast.LENGTH_SHORT).show()
        }

    } else {
        // Legacy storage (Android 9 and below)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileNameWithExt)

        try {
            FileOutputStream(file).use { outputStream ->
                generatePdf(text, outputStream)
            }
            Toast.makeText(context, context.getString(R.string.saved_as_pdf), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


fun generatePdf(text: String, outputStream: OutputStream) {
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val textPaint = TextPaint().apply {
        color = android.graphics.Color.BLACK
        textSize = 14f
    }

    val margin = 40
    val usableWidth = pageWidth - margin * 2

    val staticLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, textPaint, usableWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(0f, 1f)
        .setIncludePad(false)
        .build()

    canvas.translate(margin.toFloat(), margin.toFloat())
    staticLayout.draw(canvas)

    document.finishPage(page)
    document.writeTo(outputStream)
    document.close()
}



fun sharePdfFromDownloads(context: Context, fileName: String) {
    val fileNameWithExt = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"

    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileNameWithExt)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_pdf)))
}







