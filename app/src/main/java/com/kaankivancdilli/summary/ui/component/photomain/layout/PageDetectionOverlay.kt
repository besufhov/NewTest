package com.kaankivancdilli.summary.ui.component.photomain.layout

import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.kaankivancdilli.summary.data.ocr.PageBoundedOcrHandler
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

        // Detection rectangle for OCR calculations
        val detectionRect = Rect(
            topLeft.x.roundToInt(),
            topLeft.y.roundToInt(),
            bottomRight.x.roundToInt(),
            bottomRight.y.roundToInt()
        )
        pageBoundedOcrHandler.updateDetectionRect(detectionRect)


        // Logs for validation
        Log.d("OCR", "🟥 Detection Rectangle: $detectionRect")
        Log.d("OCR", "🔴 Top Left: $topLeft, Top Right: $topRight")
        Log.d("OCR", "🔵 Bottom Left: $bottomLeft, Bottom Right: $bottomRight")

        // Draw custom rectangle with lines
        //     val lineColor = Color.Red
        //   val lineWidth = 8f

        //   drawLine(color = lineColor, start = topLeft, end = topRight, strokeWidth = lineWidth) // Top
        //   drawLine(color = lineColor, start = topRight, end = bottomRight, strokeWidth = lineWidth) // Right
        //  drawLine(color = lineColor, start = bottomRight, end = bottomLeft, strokeWidth = lineWidth) // Bottom
        //   drawLine(color = lineColor, start = bottomLeft, end = topLeft, strokeWidth = lineWidth) // Left
    }
}