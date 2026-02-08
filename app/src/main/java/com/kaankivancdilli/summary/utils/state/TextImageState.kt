package com.kaankivancdilli.summary.utils.state

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TextImageState {
    private val _recognizedTexts = MutableStateFlow<List<String>>(emptyList())
    val recognizedTexts: StateFlow<List<String>> = _recognizedTexts

    private val _capturedImages = MutableStateFlow<List<Triple<String, Bitmap, String>>>(emptyList())
    val capturedImages: StateFlow<List<Triple<String, Bitmap, String>>> = _capturedImages

    fun updateTextList(newTexts: List<String>) {
        _recognizedTexts.value = newTexts
    }

    fun updateCapturedImages(newImages: List<Triple<String, Bitmap, String>>) {
        _capturedImages.value = newImages
        _recognizedTexts.value = newImages.map { it.third } // ✅ Keep texts in sync
    }
}