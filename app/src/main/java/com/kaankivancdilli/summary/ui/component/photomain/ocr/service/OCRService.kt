package com.kaankivancdilli.summary.ui.component.photomain.ocr.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.net.Uri
import com.kaankivancdilli.summary.data.repository.main.photomain.OcrRepository
import com.kaankivancdilli.summary.ui.state.ocr.ResultStateOCR
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object OCRWorker {

    sealed class OcrWorkerResult {
        data class Success(val bitmap: Bitmap, val text: String) : OcrWorkerResult()
        data class Error(val message: String) : OcrWorkerResult()
    }

    private val _ocrResults = MutableSharedFlow<OcrWorkerResult>(replay = 1)
    val ocrResults = _ocrResults.asSharedFlow()

    private val workerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val processingMutex = Mutex()

    @SuppressLint("StaticFieldLeak")
    private var ocrRepository: OcrRepository? = null

    fun initialize(context: Context) {
        if (ocrRepository == null) {
            ocrRepository = OcrRepository(context.applicationContext)
        }
    }

    fun processImage(uri: Uri) {
        val repo = ocrRepository ?: run {
            workerScope.launch {
                _ocrResults.emit(OcrWorkerResult.Error("OCRWorker not initialized"))
            }
            return
        }

        workerScope.launch {
            processingMutex.withLock {
                try {
                    val (bitmapResult, ocrResult) = repo.processUriWithTilingSafe(uri)

                    if (bitmapResult is ResultStateOCR.Success<*> && ocrResult is ResultStateOCR.Success<*>) {
                        val bitmap = bitmapResult as ResultStateOCR.Success<Bitmap>
                        val text = ocrResult as ResultStateOCR.Success<String>

                        _ocrResults.emit(OcrWorkerResult.Success(bitmap.data, text.data))
                    } else {
                        val message = bitmapResult.errorMessage() + " " + ocrResult.errorMessage()
                        _ocrResults.emit(OcrWorkerResult.Error(message.trim()))
                    }
                } catch (oom: OutOfMemoryError) {
                    _ocrResults.emit(OcrWorkerResult.Error("Image too large for this device"))
                } catch (e: Exception) {
                    _ocrResults.emit(OcrWorkerResult.Error("Unexpected error: ${e.message}"))
                }
            }
        }
    }

    fun close() {
        workerScope.launch {
            try { ocrRepository?.closeRecognizer() } catch (_: Throwable) {}
        }
        workerScope.cancel()
    }

    private fun <T> ResultStateOCR<T>.errorMessage(): String = when (this) {
        is ResultStateOCR.Error -> this.message
        else -> ""
    }
}