package com.kaankivancdilli.summary.core.mapper.anything

import android.content.Context
import com.kaankivancdilli.summary.R

object TypeMapper {

    fun toEnglishKey(context: Context, translatedType: String): String {
        return when (translatedType) {
            context.getString(R.string.tv_show) -> "tv_show"
            context.getString(R.string.book) -> "book"
            context.getString(R.string.movie) -> "movie"
            context.getString(R.string.article) -> "article"
            context.getString(R.string.biography) -> "biography"
            context.getString(R.string.anime) -> "anime"
            else -> "unknown"
        }
    }
}