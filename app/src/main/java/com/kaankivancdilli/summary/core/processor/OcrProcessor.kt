package com.kaankivancdilli.summary.core.processor

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import com.kaankivancdilli.summary.core.handler.photomain.PageBoundedOcrHandler
import com.kaankivancdilli.summary.data.repository.main.photomain.OcrRepository
import com.kaankivancdilli.summary.utils.bitmap.downsampleToMaxSize
import com.kaankivancdilli.summary.utils.bitmap.rotate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OcrProcessor @Inject constructor(
    private val ocrRepository: OcrRepository,
    private val pageBoundedOcrHandler: PageBoundedOcrHandler,
) {

    private var lastAnalyzedTime = 0L
    private val ocrThrottleMs = 1000L
    private var ocrJob: Job? = null

    @OptIn(ExperimentalGetImage::class)
    fun processImage(
        imageProxy: ImageProxy,
        previewView: PreviewView,
        fillView: View?,
        scope: CoroutineScope,
        onTextReady: (String, Bitmap) -> Unit
    ) {
        val now = System.currentTimeMillis()
        if (now - lastAnalyzedTime < ocrThrottleMs) {
            imageProxy.close()
            return
        }
        lastAnalyzedTime = now

        pageBoundedOcrHandler.updatePreviewDimensions(
            previewView.width,
            previewView.height
        )

        val detectionRect = pageBoundedOcrHandler.getDetectionRect()
        if (detectionRect == null) {
            imageProxy.close()
            return
        }

        ocrJob?.cancel()
        ocrJob = scope.launch(Dispatchers.Default) {
            try {
                val bitmap = imageProxy.toBitmap()
                    .rotate(90f)
                    .downsampleToMaxSize(10)

                val text = ocrRepository
                    .recognizeTextFromBitmap(bitmap)
                    .toString()

                withContext(Dispatchers.Main) {
                    onTextReady(text, bitmap)
                }
            } catch (e: Exception) {
                Log.e("OCR", "❌ OCR error: ${e.message}")
            } finally {
                imageProxy.close()
            }
        }
    }
}