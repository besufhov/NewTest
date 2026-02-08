package com.kaankivancdilli.summary.ui.camera

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import android.util.Log
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit
import android.view.View
import androidx.camera.core.ImageAnalysis


class CameraHandler(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val processImage: (ImageProxy) -> Unit
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    fun initializeCamera(previewView: PreviewView, fillView: View?): PreviewView {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply { setAnalyzer(ContextCompat.getMainExecutor(context), processImage) }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)

                Log.d("CameraHandler", "📷 Camera successfully started: $camera")
            } catch (e: Exception) {
                Log.e("CameraHandler", "❌ Camera initialization failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))

        return previewView
    }

    fun triggerFocus() {
        camera?.cameraControl?.startFocusAndMetering(
            FocusMeteringAction.Builder(
                SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(0.5f, 0.5f)
            ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
        )
    }
}