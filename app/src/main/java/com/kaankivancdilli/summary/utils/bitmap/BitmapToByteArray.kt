package com.kaankivancdilli.summary.utils.bitmap

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()

    val scaledBitmap = Bitmap.createScaledBitmap(
        bitmap,
        bitmap.width / 3,
        bitmap.height / 3,
        true
    )

    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)

    return stream.toByteArray()
}