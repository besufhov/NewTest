package com.kaankivancdilli.summary.ui.utils.upload

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.utils.documents.extraction.docx.extractTextFromDocx
import com.kaankivancdilli.summary.ui.utils.documents.extraction.pdf.extractTextFromPdf
import com.kaankivancdilli.summary.ui.utils.documents.extraction.txt.extractTextFromTxt
import com.kaankivancdilli.summary.ui.viewmodel.main.textadd.TextAddViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// File upload and handling
import kotlinx.coroutines.*
import java.util.concurrent.Executors

@SuppressLint("StringFormatMatches")
fun handleFileUpload(
    context: Context,
    uri: Uri,
    viewModel: TextAddViewModel,
    onComplete: () -> Unit
) {
    // Custom dispatcher with 2 threads
    val extractionDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Check file size before extracting
            val fileSize = getFileSize(context, uri)
            val maxFileSizeMB = 7 * 1024 * 1024 // 7 MB

            if (fileSize > maxFileSizeMB * 1024L * 1024L) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.high_megabyte_file, maxFileSizeMB),
                        Toast.LENGTH_LONG
                    ).show()
                    onComplete()
                }
                return@launch
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->

                val fileExtension = getFileExtension(uri, context)

                // Use withTimeoutOrNull to cancel after 60 sec max
                val extractedText = withContext(extractionDispatcher) {
                    withTimeoutOrNull(60_000L) {
                        when (fileExtension) {
                            "pdf" -> extractTextFromPdf(inputStream)
                            "docx" -> extractTextFromDocx(inputStream)
                            "txt" -> extractTextFromTxt(inputStream)
                            else -> null
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (extractedText == null) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.timed_out),
                            Toast.LENGTH_SHORT
                        ).show()
                        onComplete()
                        return@withContext
                    }

                    val maxLength = 16000
                    val safeText = if (extractedText.length > maxLength) {
                        extractedText.substring(0, maxLength)
                    } else {
                        extractedText
                    }

                    if (safeText.isNotBlank()) {
                        viewModel.saveText(safeText)
                        Toast.makeText(
                            context,
                            context.getString(R.string.extracted_text_length, safeText.length),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        Toast.makeText(context, context.getString(R.string.no_text_extracted), Toast.LENGTH_SHORT).show()
                    }
                    onComplete()
                }

            } ?: withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.input_stream), Toast.LENGTH_SHORT).show()
                onComplete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.error_reading_file), Toast.LENGTH_SHORT).show()
                onComplete()
            }
        } finally {
            extractionDispatcher.close()
        }
    }
}

fun getFileExtension(uri: Uri, context: Context): String {
    val mimeType = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
}

// Helper function to get file size from Uri
fun getFileSize(context: Context, uri: Uri): Long {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
        cursor.moveToFirst()
        cursor.getLong(sizeIndex)
    } ?: -1L
}
