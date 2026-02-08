package com.kaankivancdilli.summary.data.ocr.repository

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kaankivancdilli.summary.R
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class OcrRepository(val context: Context) {

    val mlRecognizer: TextRecognizer
    private val tesseractHelper: TesseractOcrHelper?

    init {
        val languageCode = Locale.getDefault().language
        mlRecognizer = when (languageCode) {
            "ja" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            "zh" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            "ko" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            "hi", "mr", "ne" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        }

        tesseractHelper = when (languageCode) {
            "ar", "ru", "he", "th", "fa", "iw", "el", "bn", "uk", "ta", "te" -> TesseractOcrHelper(context, languageCode)
            else -> null
        }

        Log.d("OcrRepository", "📣 Recognizer initialized for language: $languageCode")
    }

    private fun tessBaseSafe(bitmap: Bitmap): String {
        return tesseractHelper?.recognizeTextFromBitmap(bitmap)
            ?: throw IllegalStateException("Tesseract helper is null")
    }

    suspend fun processUriWithTilingSafe(
        uri: Uri,
        maxTileSize: Int = 1024,         // base tile size
        maxDim: Int = 2000,               // max width/height for safeBitmap
        maxPixels: Long = 8_000_000,      // max total pixels
        maxFileSizeMB: Int = 12           // reject files >12MB early
    ): Pair<ResultStateOCR<Bitmap>, ResultStateOCR<String>> {

        val corruptedMsg = context.getString(R.string.corrupted_image)
        val tooBigMsg = context.getString(R.string.high_megabyte_image, maxFileSizeMB)
        val errorDecodingMsg = context.getString(R.string.error_decoding_image)
        val options = BitmapFactory.Options() // declare outside

        try {
            // 0️⃣ Quick file size check
            val fileSizeMB = context.contentResolver.openFileDescriptor(uri, "r")?.use {
                it.statSize.div(1024 * 1024)
            } ?: 0
            if (fileSizeMB > maxFileSizeMB) {
                return ResultStateOCR.Error(tooBigMsg) to ResultStateOCR.Error(tooBigMsg)
            }

            // 1️⃣ Decode bounds only
            options.inJustDecodeBounds = true
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            val (origW, origH) = options.outWidth to options.outHeight
            if (origW <= 0 || origH <= 0) return ResultStateOCR.Error(corruptedMsg) to ResultStateOCR.Error(corruptedMsg)

            // 2️⃣ Calculate safe downscale factor
            val scaleByDim = maxOf(origW.toFloat() / maxDim, origH.toFloat() / maxDim)
            val scaleByPixels = sqrt(origW.toLong() * origH.toLong() / maxPixels.toFloat())
            val finalScale = max(scaleByDim, scaleByPixels).coerceAtLeast(1f)

            // Round sampleSize to nearest power of 2 for memory safety
            val sampleSize = 1 shl ceil(log2(finalScale.toDouble())).toInt()

            // 3️⃣ Use BitmapRegionDecoder
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return ResultStateOCR.Error(corruptedMsg) to ResultStateOCR.Error(corruptedMsg)

            val decoder: BitmapRegionDecoder = try {
                FileInputStream(parcelFileDescriptor.fileDescriptor).use { fis ->
                    BitmapRegionDecoder.newInstance(fis, false) // still works, deprecated
                } ?: throw IOException("Failed to create BitmapRegionDecoder")
            } catch (e: Exception) {
                parcelFileDescriptor.close()
                return ResultStateOCR.Error("$corruptedMsg ${e.message}") to ResultStateOCR.Error("$corruptedMsg ${e.message}")
            }

            // Close the ParcelFileDescriptor explicitly after decoder is created
            parcelFileDescriptor.close()

            // 4️⃣ Determine safe bitmap size
            val safeW = (origW / sampleSize).coerceAtMost(maxDim)
            val safeH = (origH / sampleSize).coerceAtMost(maxDim)
            val safeBitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(safeBitmap)

            // Optional: dynamic tile size for low-memory devices
            val memClass = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
            val safeTileSize = if (memClass < 128) min(maxTileSize, 512) else maxTileSize

            val resultBuilder = StringBuilder()
            var y = 0
            while (y < decoder.height) {
                var x = 0
                val tileHeight = min(safeTileSize, decoder.height - y)
                while (x < decoder.width) {
                    val tileWidth = min(safeTileSize, decoder.width - x)
                    val rect = Rect(x, y, x + tileWidth, y + tileHeight)

                    // 5️⃣ Decode tile only
                    val tile = decoder.decodeRegion(rect, BitmapFactory.Options().apply { inSampleSize = sampleSize })
                    tile?.let {
                        // 6️⃣ OCR on tile
                        val text = if (tesseractHelper != null) {
                            try { tessBaseSafe(it) } catch (_: Throwable) { "" }
                        } else {
                            try {
                                val inputImage = InputImage.fromBitmap(it, 0)
                                mlRecognizer.process(inputImage).await().text
                            } catch (_: Throwable) { "" }
                        }
                        if (text.isNotBlank()) resultBuilder.append(text).append("\n")

                        // 7️⃣ Draw tile into safeBitmap for UI
                        val destRect = Rect(
                            (x / sampleSize),
                            (y / sampleSize),
                            (x + tileWidth) / sampleSize,
                            (y + tileHeight) / sampleSize
                        )
                        canvas.drawBitmap(it, null, destRect, null)
                        it.recycle()
                    }
                    x += tileWidth
                }
                y += tileHeight
            }

            val finalText = resultBuilder.toString().trim()
            val textResult = if (finalText.isBlank()) ResultStateOCR.Error("OCR skipped (empty)") else ResultStateOCR.Success(finalText)

            return ResultStateOCR.Success(safeBitmap) to textResult

        } catch (oom: OutOfMemoryError) {
            Log.e("OCR", "OOM decoding image ${uri}, original size: ${options.outWidth}x${options.outHeight}")
            return ResultStateOCR.Error("Image too large for this device") to ResultStateOCR.Error("Image too large for this device")
        } catch (e: Exception) {
            return ResultStateOCR.Error("$errorDecodingMsg ${e.message}") to ResultStateOCR.Error("$errorDecodingMsg ${e.message}")
        }
    }



    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): ResultStateOCR<String> {
        return try {
            if (tesseractHelper != null) {
                withTimeoutOrNull(10_000) { tessBaseSafe(bitmap) }?.let { ResultStateOCR.Success(it) }
                    ?: ResultStateOCR.Error("OCR skipped (timeout)")
            } else {
                val text = mlRecognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text
                if (text.isBlank()) ResultStateOCR.Error("OCR skipped (empty)")
                else ResultStateOCR.Success(text)
            }
        } catch (e: Exception) {
            Log.e("OcrRepository", "❌ OCR failed: ${e.message}")
            ResultStateOCR.Error("OCR skipped (error)")
        }
    }

    suspend fun closeRecognizer() {
        mlRecognizer.close()
        tesseractHelper?.close()
        Log.d("OcrRepository", "🧹 OCR resources released")
    }
}

sealed class ResultStateOCR<out T> {
    data class Success<out T>(val data: T) : ResultStateOCR<T>()
    data class Error(val message: String) : ResultStateOCR<Nothing>()
}


