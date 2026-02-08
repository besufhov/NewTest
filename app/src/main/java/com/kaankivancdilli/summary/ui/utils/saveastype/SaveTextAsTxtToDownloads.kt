package com.kaankivancdilli.summary.ui.utils.saveastype

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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.kaankivancdilli.summary.R
import java.io.File
import java.io.FileOutputStream

fun saveTextAsTxtToDownloads(context: Context, fileName: String, text: String) {
    val fileNameWithExt = "$fileName.txt"

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    // Ask runtime permission on Android 6–9
    if (Build.VERSION.SDK_INT in 23..28 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
    ) {
        val activity = context.findActivity()
        if (activity != null) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1001
            )
        } else {
            Toast.makeText(context, "Permission request requires an Activity", Toast.LENGTH_SHORT).show()
        }
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // ✅ Scoped storage (Android 10+)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameWithExt)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = MediaStore.Files.getContentUri("external")?.let {
            resolver.insert(it, contentValues)
        }

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(text.toByteArray())
            }
            Toast.makeText(context, context.getString(R.string.saved_as_text), Toast.LENGTH_SHORT).show()
        }

    } else {
        // ✅ Legacy storage (Android 9 and below)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileNameWithExt)

        try {
            FileOutputStream(file).use { it.write(text.toByteArray()) }
            Toast.makeText(context, context.getString(R.string.saved_as_text), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save TXT: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}



fun shareTxtFromDownloads(context: Context, fileName: String) {
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_text)))

}
