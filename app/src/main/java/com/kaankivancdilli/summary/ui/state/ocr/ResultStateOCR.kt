package com.kaankivancdilli.summary.ui.state.ocr

sealed class ResultStateOCR<out T> {
    data class Success<out T>(val data: T) : ResultStateOCR<T>()
    data class Error(val message: String) : ResultStateOCR<Nothing>()
}