package com.kaankivancdilli.summary.core.domain.handler.photomain

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import javax.inject.Inject

@ViewModelScoped
class PageBoundedOcrHandler @Inject constructor(
    @ApplicationContext context: Context
) {
    private val detectionRectState = MutableStateFlow<Rect?>(null)
    private var previewWidth = 0
    private var previewHeight = 0
    private val _recognizedText = MutableStateFlow("")

    val recognizedText: StateFlow<String> = _recognizedText

    fun getDetectionRect(): Rect? = detectionRectState.value

    fun updateDetectionRect(rect: Rect) {
        detectionRectState.value = rect
        Log.d("OCR", "📏 Updated detection rectangle: $rect")
    }

    fun updatePreviewDimensions(width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
        Log.d("OCR", "📐 Updated preview size: $width x $height")
    }

    /** ✅ Crops ImageProxy to Detection Rect */
    fun cropToDetectionRect(image: ImageProxy): InputImage? {
        logImageFormat(image)
        val detectionRect = detectionRectState.value ?: return null

        if (previewWidth == 0 || previewHeight == 0) {
            Log.e("OCR", "❌ Invalid preview dimensions: $previewWidth x $previewHeight")
            return null
        }

        // Calculate the center rectangle relative to the image size
        val centerX = previewWidth / 2
        val centerY = previewHeight / 2

        // Define the dimensions of the rectangle (you can adjust these percentages as needed)
        val rectWidth = previewWidth * 0.75f  // 75% of the width
        val rectHeight = previewHeight * 0.45f  // 45% of the height

        // Calculate the rectangle's coordinates (centered on the preview)
        val left = (centerX - rectWidth / 2).toInt()
        val top = (centerY - rectHeight / 2).toInt()
        val right = (centerX + rectWidth / 2).toInt()
        val bottom = (centerY + rectHeight / 2).toInt()

        // Ensure corrected top and bottom
        val correctedTop = top.coerceAtMost(bottom)
        val correctedBottom = bottom.coerceAtLeast(top)

        // Ensure corrected left and right
        val correctedLeft = left.coerceAtMost(right)
        val correctedRight = right.coerceAtLeast(left)

        val cropRect = Rect(correctedLeft, correctedTop, correctedRight, correctedBottom)

        // Validate crop rect before proceeding
        if (cropRect.width() <= 0 || cropRect.height() <= 0) {
            Log.e("OCR", "❌ Invalid crop rect: $cropRect")
            return null  // Abort if the crop rect is invalid
        }

        // If the image is rotated, swap width and height to correctly crop the area
        val isRotated = image.imageInfo.rotationDegrees == 90 || image.imageInfo.rotationDegrees == 270

        // Adjust the crop rectangle for rotation
        val finalRect = if (isRotated) {
            // Adjusted calculation for rotated images
            val left = (cropRect.top * image.height / previewHeight).toInt().coerceIn(0, image.width)
            val top = (cropRect.left * image.width / previewWidth).toInt().coerceIn(0, image.height)
            val right = (cropRect.bottom * image.height / previewHeight).toInt().coerceIn(left, image.width)
            val bottom = (cropRect.right * image.width / previewWidth).toInt().coerceIn(top, image.height)

            Rect(left, top, right, bottom)
        } else {
            // Normal scaling without rotation
            val left = (cropRect.left * image.width / previewWidth).toInt().coerceIn(0, image.width)
            val top = (cropRect.top * image.height / previewHeight).toInt().coerceIn(0, image.height)
            val right = (cropRect.right * image.width / previewWidth).toInt().coerceIn(left, image.width)
            val bottom = (cropRect.bottom * image.height / previewHeight).toInt().coerceIn(top, image.height)

            Rect(left, top, right, bottom)
        }

        // ✅ Debugging Logs
        Log.d("OCR", "📏 Detection Rect (Preview): $detectionRect")
        Log.d("OCR", "📐 Crop Rect (Image): $cropRect")
        Log.d("OCR", "📷 Image Size: ${image.width} x ${image.height}")
        Log.d("OCR", "🔄 Image Rotation: ${image.imageInfo.rotationDegrees}")
        Log.d("OCR", "📊 Is Rotated: $isRotated")
        Log.d("OCR", "📊 Final Rect: $finalRect")

        return try {
            imageProxyToInputImage(image)?.also {
                Log.w("OCR", "⚠️ Using fallback InputImage (no cropping applied).")
            }
        } catch (e: Exception) {
            Log.e("OCR", "❌ Image cropping failed: ${e.message}")
            null
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun createInputImageFromImageProxy(imageProxy: ImageProxy, cropRect: Rect): InputImage? {
        val mediaImage = imageProxy.image ?: return null
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        Log.d("OCR", "Crop Rect: $cropRect, Width: ${cropRect.width()}, Height: ${cropRect.height()}")
        if (cropRect.isEmpty || cropRect.left < 0 || cropRect.top < 0 ||
            cropRect.right > mediaImage.width || cropRect.bottom > mediaImage.height
        ) {
            Log.e("OCR", "❌ Invalid crop rect: $cropRect")
            return null
        }

        val croppedYBuffer = cropPlane(imageProxy.planes[0].buffer, cropRect, mediaImage.width)
        val croppedUBuffer = cropPlane(imageProxy.planes[1].buffer, cropRect, mediaImage.width / 2)
        val croppedVBuffer = cropPlane(imageProxy.planes[2].buffer, cropRect, mediaImage.width / 2)

        Log.d("OCR", "✅ Cropped Y Buffer size: ${croppedYBuffer.remaining()}")
        Log.d("OCR", "✅ Cropped U Buffer size: ${croppedUBuffer.remaining()}")
        Log.d("OCR", "✅ Cropped V Buffer size: ${croppedVBuffer.remaining()}")

        // Combine buffers
        val combinedBuffer = ByteBuffer.allocate(
            croppedYBuffer.remaining() + croppedUBuffer.remaining() + croppedVBuffer.remaining()
        )

        Log.d("OCR", "✅ Combined Buffer capacity: ${combinedBuffer.capacity()}")

        putPlanesIntoCombinedBuffer(croppedYBuffer, croppedUBuffer, croppedVBuffer, combinedBuffer)

        return try {
            InputImage.fromByteBuffer(
                combinedBuffer,
                cropRect.width(),
                cropRect.height(),
                rotationDegrees,
                InputImage.IMAGE_FORMAT_NV21
            )
        } catch (e: Exception) {
            Log.e("OCR", "❌ Error creating InputImage: ${e.message}")
            null
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun imageProxyToInputImage(imageProxy: ImageProxy): InputImage? {
        val mediaImage = imageProxy.image ?: return null
        return InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    }

    private fun putPlanesIntoCombinedBuffer(yPlane: ByteBuffer, uPlane: ByteBuffer, vPlane: ByteBuffer, combinedBuffer: ByteBuffer) {
        try {
            yPlane.rewind()
            uPlane.rewind()
            vPlane.rewind()

            combinedBuffer.clear()
            combinedBuffer.put(yPlane).put(uPlane).put(vPlane)
            combinedBuffer.flip()

            Log.d("OCR", "✅ Combined buffer flipped: position = ${combinedBuffer.position()}, limit = ${combinedBuffer.limit()}")
        } catch (e: Exception) {
            Log.e("OCR", "❌ Error writing to combined buffer", e)
        }
    }

    private fun cropPlane(byteBuffer: ByteBuffer, cropRect: Rect, rowStride: Int): ByteBuffer {
        val croppedBuffer = ByteBuffer.allocate(cropRect.width() * cropRect.height())
        for (row in 0 until cropRect.height()) {
            val position = row * rowStride + cropRect.left
            byteBuffer.position(position)
            val rowBytes = ByteArray(cropRect.width())
            byteBuffer.get(rowBytes, 0, cropRect.width())
            croppedBuffer.put(rowBytes)
        }
        croppedBuffer.flip()
        return croppedBuffer
    }

    fun logImageFormat(image: ImageProxy) {
        when (image.format) {
            ImageFormat.YUV_420_888 -> Log.d("OCR", "✅ Image format: YUV_420_888")
            ImageFormat.RGB_565 -> Log.d("OCR", "✅ Image format: RGB_565")
            else -> Log.d("OCR", "⚠️ Image format: Unknown (${image.format})")
        }
    }
}













