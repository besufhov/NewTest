package com.kaankivancdilli.summary.data.ocr.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.min

class TesseractOcrHelper(
    val context: Context,
    languageCode: String = Locale.getDefault().language
) {

    private val tessBaseAPI: TessBaseAPI
    private val tessLangCode: String
    private val dataPath: String

    init {
        tessLangCode = when (languageCode) {
            "ru" -> "rus"
            "ar" -> "ara"
            "fa" -> "fas"
            "he", "iw" -> "heb"
            "th" -> "tha"
            "el" -> "ell"
            "bn" -> "ben"
            "uk" -> "ukr"
            "ta" -> "tam"
            "te" -> "tel"
            else -> languageCode
        }

        dataPath = "${context.filesDir}/tesseract/"
        val tessdataDir = File("$dataPath/tessdata")
        if (!tessdataDir.exists()) tessdataDir.mkdirs()

        // Copy traineddata if missing
        val trainedDataFile = File(tessdataDir, "$tessLangCode.traineddata")
        if (!trainedDataFile.exists()) {
            context.assets.open("tessdata/$tessLangCode.traineddata").use { input ->
                FileOutputStream(trainedDataFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        // Init Tesseract
        tessBaseAPI = TessBaseAPI()
        if (!tessBaseAPI.init(dataPath, tessLangCode)) {
            tessBaseAPI.recycle()
            throw IllegalStateException("Tesseract init failed for lang=$tessLangCode")
        }

        tessBaseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK
        Log.d("TesseractOcrHelper", "✅ Tesseract initialized ($tessLangCode)")
    }

    fun recognizeTextFromBitmap(bitmap: Bitmap): String {
        tessBaseAPI.setImage(bitmap)
        return tessBaseAPI.utF8Text ?: ""
    }

    fun close() {
        try { tessBaseAPI.clear() } catch (_: Throwable) {}
        tessBaseAPI.recycle()
        Log.d("TesseractOcrHelper", "🧹 Tesseract recycled")
    }
}

