package com.kaankivancdilli.summary.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context
import android.view.View
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.max
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.net.Uri
import android.widget.Toast
import androidx.annotation.WorkerThread
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.kaankivancdilli.summary.data.ocr.PageBoundedOcrHandler
import com.kaankivancdilli.summary.data.ocr.repository.OCRWorker
import com.kaankivancdilli.summary.data.ocr.repository.OcrRepository
import com.kaankivancdilli.summary.data.ocr.repository.ResultStateOCR
import com.kaankivancdilli.summary.utils.state.TextImageState
import dagger.hilt.android.internal.Contexts.getApplication
import java.util.Locale


class OcrViewModel(
    private val ocrRepository: OcrRepository,
    private val pageBoundedOcrHandler: PageBoundedOcrHandler,
    private val textImageState: TextImageState,
) : ViewModel() {

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val recognizer = ocrRepository.mlRecognizer

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var isOcrActive = true

    private val _capturedImage = MutableSharedFlow<Bitmap?>()
    val capturedImage = _capturedImage.asSharedFlow()

    private var ocrJob: Job? = null
    private var lastAnalyzedTime = 0L
    private val ocrThrottleMs = 1000L // 1 second throttle

    init {
        viewModelScope.launch {
            OCRWorker.ocrResults.collect { result ->
                when (result) {
                    is OCRWorker.OcrWorkerResult.Success -> {
                        processOcrResult(result.text)
                        emitCapturedImage(result.bitmap)
                    }
                    is OCRWorker.OcrWorkerResult.Error -> {
                        Log.e("OCR", "OCR Error: ${result.message}")
                    }
                }
            }
        }

    }

    suspend fun emitCapturedImage(bitmap: Bitmap?) {
        _capturedImage.emit(bitmap)
    }

    //==========================
    // Bitmap size helpers
    //==========================
    private fun calculateBitmapSizeMB(bitmap: Bitmap): Int {
        return bitmap.byteCount / (1024 * 1024)
    }

    private fun Bitmap.downsampleToMaxSize(maxSizeMB: Int = 20): Bitmap {
        var bmp = this
        while (calculateBitmapSizeMB(bmp) > maxSizeMB) {
            val newWidth = (bmp.width * 0.8).toInt()
            val newHeight = (bmp.height * 0.8).toInt()
            bmp = Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true)
        }
        return bmp
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        if (degrees == 0f) return this
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    //==========================
// Gallery / Selected Image
//==========================
    fun processImage(context: Context, uri: Uri) {
        OCRWorker.initialize(context.applicationContext)
        OCRWorker.processImage(uri)
    }

    //==========================
    // OCR Processing
    //==========================
    fun processOcrResult(text: String) {
        Log.d("OCR", "📏 Detection Rectangle: ${pageBoundedOcrHandler.getDetectionRect()}")
        _recognizedText.value = text
        val updatedTexts = textImageState.recognizedTexts.value + text
        textImageState.updateTextList(updatedTexts)
        Log.d("OCR", "✅ Text updated: $text")
        pauseOcr()
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy, previewView: PreviewView, fillView: View?) {
        if (!isOcrActive) {
            imageProxy.close()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastAnalyzedTime < ocrThrottleMs) {
            imageProxy.close()
            return
        }
        lastAnalyzedTime = now

        imageProxy.image?.let { _ ->
            val imageWidth = imageProxy.width
            val imageHeight = imageProxy.height
            val previewWidth = previewView.width
            val previewHeight = previewView.height

            pageBoundedOcrHandler.updatePreviewDimensions(previewWidth, previewHeight)
            val previewRatio = imageWidth.toFloat() / imageHeight.toFloat()
            fillView?.let { updateFillView(previewRatio, previewView, it) }

            val detectionRect = pageBoundedOcrHandler.getDetectionRect()
            if (detectionRect != null) {
                val croppedImage = pageBoundedOcrHandler.cropToDetectionRect(imageProxy)
                croppedImage?.let { _ ->
                    ocrJob?.cancel()
                    ocrJob = viewModelScope.launch(Dispatchers.Default) {
                        try {
                            val bitmap = imageProxy.toBitmap().rotate(90f)

                            val safeBitmap = bitmap.downsampleToMaxSize(10) // ✅ enforce ≤10MB
                            val text = ocrRepository.recognizeTextFromBitmap(safeBitmap)
                            withContext(Dispatchers.Main) {
                                processOcrResult(text.toString())
                                emitCapturedImage(safeBitmap)
                            }
                        } catch (e: Exception) {
                            Log.e("OCR", "❌ OCR error: ${e.message}")
                        } finally {
                            imageProxy.close()
                        }
                    }
                } ?: imageProxy.close()
            } else {
                imageProxy.close()
            }
        } ?: imageProxy.close()
    }

    inline fun ImageProxy.safeUse(block: (ImageProxy) -> Unit) {
        try {
            block(this)
        } finally {
            close()
        }
    }

    private fun updateFillView(previewRatio: Float, previewView: PreviewView, fillView: View) {
        val previewHeight = previewView.height
        val expectedHeight = (previewView.width / previewRatio).toInt()
        val blankSpace = max(0, previewHeight - expectedHeight)
        fillView.layoutParams = fillView.layoutParams.apply { height = blankSpace }
        fillView.visibility = if (blankSpace > 0) View.VISIBLE else View.GONE
    }

    //==========================
    // CameraX Setup
    //==========================
    fun createImageAnalyzer(context: Context, previewView: PreviewView, fillView: View?): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
                    .build()
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    processImage(imageProxy, previewView, fillView)
                }
            }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun initializeCamera(context: Context, lifecycleOwner: LifecycleOwner, fillView: View?): PreviewView {
        val previewView = PreviewView(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val previewBuilder = Preview.Builder()
                val camera2Interop = Camera2Interop.Extender(previewBuilder)
                camera2Interop.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                val resolutionSelector = ResolutionSelector.Builder().build()
                previewBuilder.setResolutionSelector(resolutionSelector)
                previewView.scaleType = PreviewView.ScaleType.FIT_START
                val preview = previewBuilder.build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val imageAnalyzer = createImageAnalyzer(context, previewView, fillView)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
                isOcrActive = false
                Log.d("OCR", "📷 Camera successfully started: $camera")
            } catch (e: Exception) {
                Log.e("OCR", "❌ Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
        return previewView
    }

    fun triggerFocusAndResume() {
        camera?.cameraControl?.startFocusAndMetering(
            FocusMeteringAction.Builder(
                SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(0.5f, 0.5f)
            ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
        )
        if (!isOcrActive) resumeOcr()
    }

    private fun pauseOcr() {
        if (isOcrActive) {
            isOcrActive = false
            Log.d("OCR", "⏸️ OCR Paused")
        }
    }

    private fun resumeOcr() {
        if (!isOcrActive) {
            isOcrActive = true
            Log.d("OCR", "▶️ OCR Resumed")
        }
    }

    override fun onCleared() {
        super.onCleared()
        ocrJob?.cancel()
        viewModelScope.launch {
            ocrRepository.closeRecognizer()
        }
        Log.d("OCR", "🧹 OCR resources released")
    }
}

































