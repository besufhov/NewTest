package com.kaankivancdilli.summary.utils.state.texttoimage

import android.graphics.Bitmap
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ViewModelScoped
class TextImageState @Inject constructor() {
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