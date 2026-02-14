package com.kaankivancdilli.summary.ui.viewmodel.sub.sharedimage

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SharedImageViewModel @Inject constructor(): ViewModel() {
    private val _imageData = MutableStateFlow<List<Triple<String, Bitmap, String>>?>(null)
    val imageData: StateFlow<List<Triple<String, Bitmap, String>>?> = _imageData

    fun setImageData(images: List<Triple<String, Bitmap, String>>?) {
        Log.d("SharedImageViewModel", "Setting image data: ${images?.size}")
        _imageData.value = images.takeIf { it?.isNotEmpty() == true } ?: emptyList()
    }

    fun clearImageData() {
        _imageData.value = null
    }
}