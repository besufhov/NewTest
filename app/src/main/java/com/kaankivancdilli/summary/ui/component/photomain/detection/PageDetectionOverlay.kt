package com.kaankivancdilli.summary.ui.component.photomain.detection

import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.kaankivancdilli.summary.core.domain.handler.photomain.PageBoundedOcrHandler
import kotlin.math.roundToInt

@Composable
fun PageDetectionOverlay(pageBoundedOcrHandler: PageBoundedOcrHandler) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        val rectWidth = size.width * 0.75f
        val rectHeight = size.height * 0.45f

        val topLeft = Offset((centerX - rectWidth / 2), (centerY - rectHeight / 2))
        val topRight = Offset((centerX + rectWidth / 2), (centerY - rectHeight / 2))
        val bottomLeft = Offset((centerX - rectWidth / 2), (centerY + rectHeight / 2))
        val bottomRight = Offset((centerX + rectWidth / 2), (centerY + rectHeight / 2))

        val detectionRect = Rect(
            topLeft.x.roundToInt(),
            topLeft.y.roundToInt(),
            bottomRight.x.roundToInt(),
            bottomRight.y.roundToInt()
        )
        pageBoundedOcrHandler.updateDetectionRect(detectionRect)

        Log.d("OCR", "🟥 Detection Rectangle: $detectionRect")
        Log.d("OCR", "🔴 Top Left: $topLeft, Top Right: $topRight")
        Log.d("OCR", "🔵 Bottom Left: $bottomLeft, Bottom Right: $bottomRight")

    }
}