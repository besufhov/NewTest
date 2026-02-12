package com.kaankivancdilli.summary.utils.bitmap

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.downsampleToMaxSize(maxSizeMB: Int): Bitmap {
    var bmp = this
    while (bmp.byteCount / (1024 * 1024) > maxSizeMB) {
        bmp = Bitmap.createScaledBitmap(
            bmp,
            (bmp.width * 0.8).toInt(),
            (bmp.height * 0.8).toInt(),
            true
        )
    }
    return bmp
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    if (degrees == 0f) return this
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}