package com.kaankivancdilli.summary.ui.component.photomain.ocr.cameracontroller

import android.content.Context
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class OcrCameraController @Inject constructor() {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var isOcrActive = true

    fun createImageAnalyzer(
        context: Context,
        previewView: PreviewView,
        fillView: View?,
        analyzer: (ImageProxy, PreviewView, View?) -> Unit
    ): ImageAnalysis =
        ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(ContextCompat.getMainExecutor(context)) {
                    if (isOcrActive) analyzer(it, previewView, fillView)
                    else it.close()
                }
            }

    @OptIn(ExperimentalCamera2Interop::class)
    fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        fillView: View?,
        analyzerFactory: (Context, PreviewView, View?) -> ImageAnalysis
    ): PreviewView {
        val previewView = PreviewView(context)
        val future = ProcessCameraProvider.getInstance(context)

        future.addListener({
            cameraProvider = future.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val analyzer = analyzerFactory(context, previewView, fillView)
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analyzer
            )
            isOcrActive = false
            Log.d("OCR", "📷 Camera started")
        }, ContextCompat.getMainExecutor(context))

        return previewView
    }

    fun triggerFocusAndResume() {
        camera?.cameraControl?.startFocusAndMetering(
            FocusMeteringAction.Builder(
                SurfaceOrientedMeteringPointFactory(1f, 1f)
                    .createPoint(0.5f, 0.5f)
            ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
        )
        resumeOcr()
    }

    fun pauseOcr() {
        isOcrActive = false
        Log.d("OCR", "⏸️ OCR Paused")
    }

    private fun resumeOcr() {
        isOcrActive = true
        Log.d("OCR", "▶️ OCR Resumed")
    }
}