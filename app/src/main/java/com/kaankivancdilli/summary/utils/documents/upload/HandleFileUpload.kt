package com.kaankivancdilli.summary.utils.documents.upload

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.utils.documents.extraction.docx.extractTextFromDocx
import com.kaankivancdilli.summary.utils.documents.extraction.pdf.extractTextFromPdf
import com.kaankivancdilli.summary.utils.documents.extraction.txt.extractTextFromTxt
import com.kaankivancdilli.summary.ui.viewmodel.main.textadd.TextAddViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*
import java.util.concurrent.Executors

@SuppressLint("StringFormatMatches")
fun handleFileUpload(
    context: Context,
    uri: Uri,
    viewModel: TextAddViewModel,
    onComplete: () -> Unit
) {

    val extractionDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val fileSize = getFileSize(context, uri)
            val maxFileSizeMB = 7 * 1024 * 1024

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

fun getFileSize(context: Context, uri: Uri): Long {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
        cursor.moveToFirst()
        cursor.getLong(sizeIndex)
    } ?: -1L
}
