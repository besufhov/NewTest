package com.kaankivancdilli.summary.utils.documents.saveastype.docx

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.kaankivancdilli.summary.R
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun saveTextAsDocxToDownloads(context: Context, fileName: String, text: String) {
    val fileNameWithExt = "$fileName.docx"

    // Helper extension to unwrap ContextThemeWrapper to Activity
    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
    // Ask runtime permission on Android 6–9
    val activity = context.findActivity()
    if (Build.VERSION.SDK_INT in 23..28 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
    ) {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1003
            )
        } ?: run {
            Toast.makeText(context, "Permission request requires an Activity", Toast.LENGTH_SHORT).show()
        }
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Scoped Storage for Android 10+
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameWithExt)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")?.let {
            resolver.insert(it, contentValues)
        }

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                writeDocxToStream(text, outputStream)
            }
            Toast.makeText(context, context.getString(R.string.saved_as_docx), Toast.LENGTH_SHORT).show()
        }

    } else {
        // Legacy external storage for Android 9 and below
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileNameWithExt)

        try {
            FileOutputStream(file).use { outputStream ->
                writeDocxToStream(text, outputStream)
            }
            Toast.makeText(context, context.getString(R.string.saved_as_docx), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save DOCX: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun writeDocxToStream(text: String, outputStream: OutputStream) {
    val doc = XWPFDocument()
    val para = doc.createParagraph()
    val run = para.createRun()
    run.setText(text)
    doc.write(outputStream)
    doc.close()
}



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


