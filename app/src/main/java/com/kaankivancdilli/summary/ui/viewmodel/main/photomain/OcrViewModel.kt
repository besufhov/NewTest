package com.kaankivancdilli.summary.ui.viewmodel.main.photomain

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.PreviewView
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context
import android.view.View
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import android.net.Uri
import com.kaankivancdilli.summary.ui.component.photomain.ocr.cameracontroller.OcrCameraController
import com.kaankivancdilli.summary.ui.component.photomain.ocr.processor.OcrProcessor
import com.kaankivancdilli.summary.ui.component.photomain.ocr.handler.PageBoundedOcrHandler
import com.kaankivancdilli.summary.ui.component.photomain.ocr.service.OCRWorker
import com.kaankivancdilli.summary.data.repository.photomain.OcrRepository
import com.kaankivancdilli.summary.utils.state.texttoimage.TextImageState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class OcrViewModel @Inject constructor(
    private val ocrRepository: OcrRepository,
    private val pageBoundedOcrHandler: PageBoundedOcrHandler,
    private val textImageState: TextImageState,
    private val ocrProcessor: OcrProcessor,
    private val cameraController: OcrCameraController,
) : ViewModel() {

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _capturedImage = MutableSharedFlow<Bitmap?>()
    val capturedImage = _capturedImage.asSharedFlow()

    private var ocrJob: Job? = null

    init {
        viewModelScope.launch {
            OCRWorker.ocrResults.collect { result ->
                when (result) {
                    is OCRWorker.OcrWorkerResult.Success -> {
                        processOcrResult(result.text)
                        emitCapturedImage(result.bitmap)
                    }
                    is OCRWorker.OcrWorkerResult.Error ->
                        Log.e("OCR", "OCR Error: ${result.message}")
                }
            }
        }
    }

    suspend fun emitCapturedImage(bitmap: Bitmap?) {
        _capturedImage.emit(bitmap)
    }

    fun processImage(context: Context, uri: Uri) {
        OCRWorker.initialize(context.applicationContext)
        OCRWorker.processImage(uri)
    }

    fun processOcrResult(text: String) {
        Log.d("OCR", "📏 Detection Rectangle: ${pageBoundedOcrHandler.getDetectionRect()}")
        _recognizedText.value = text
        textImageState.updateTextList(textImageState.recognizedTexts.value + text)
        Log.d("OCR", "✅ Text updated: $text")
        cameraController.pauseOcr()
    }

    fun processImage(
        imageProxy: ImageProxy,
        previewView: PreviewView,
        fillView: View?
    ) {
        ocrProcessor.processImage(
            imageProxy,
            previewView,
            fillView,
            viewModelScope,
            onTextReady = { text, bitmap ->
                processOcrResult(text)
                viewModelScope.launch { emitCapturedImage(bitmap) }
            }
        )
    }

    fun createImageAnalyzer(
        context: Context,
        previewView: PreviewView,
        fillView: View?
    ): ImageAnalysis =
        cameraController.createImageAnalyzer(context, previewView, fillView, ::processImage)

    fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        fillView: View?
    ): PreviewView =
        cameraController.initializeCamera(context, lifecycleOwner, fillView, ::createImageAnalyzer)

    fun triggerFocusAndResume() {
        cameraController.triggerFocusAndResume()
    }

    override fun onCleared() {
        super.onCleared()
        ocrJob?.cancel()
        viewModelScope.launch { ocrRepository.closeRecognizer() }
        Log.d("OCR", "🧹 OCR resources released")
    }
}








































