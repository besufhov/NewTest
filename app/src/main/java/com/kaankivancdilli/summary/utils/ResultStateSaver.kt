package com.kaankivancdilli.summary.utils

import android.content.Context
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.state.network.ResultState

val ResultStateSaver: Saver<ResultState<String>, Any> = object : Saver<ResultState<String>, Any> {
    override fun SaverScope.save(value: ResultState<String>): Any {
        return when (value) {
            is ResultState.Idle -> {

            }
            is ResultState.Loading -> "Loading"
            is ResultState.Success -> value.data
            is ResultState.Error -> "Error:${value.message}"
        }
    }

    override fun restore(value: Any): ResultState<String> {
        return when (value) {
            "Loading" -> ResultState.Loading
            is String -> {
                if (value.startsWith("Error:")) {
                    ResultState.Error(value.removePrefix("Error:"))
                } else {
                    ResultState.Success(value)
                }
            }
            else -> ResultState.Loading
        }
    }
}

fun getLocalizedTypeFromKey(typeKey: String, context: Context): String {
    return when (typeKey) {
        "tv_show" -> context.getString(R.string.tv_show)
        "book" -> context.getString(R.string.book)
        "movie" -> context.getString(R.string.movie)
        "article" -> context.getString(R.string.article)
        else -> typeKey
    }
}