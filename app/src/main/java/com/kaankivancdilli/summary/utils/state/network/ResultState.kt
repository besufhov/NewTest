package com.kaankivancdilli.summary.utils.state.network

sealed class ResultState<out T> {
    object Idle : ResultState<Nothing>() // Add this!
    object Loading : ResultState<Nothing>()
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val message: String) : ResultState<Nothing>()
}