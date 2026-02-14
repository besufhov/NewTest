package com.kaankivancdilli.summary.ui.screens.sub.history.type

import android.content.Context
import com.kaankivancdilli.summary.R

enum class Type {
    TVSHOW, BOOK, MOVIE, ARTICLE, BIOGRAPHY, ANIME;

    fun displayName(context: Context): String {
        return when (this) {
            TVSHOW -> context.getString(R.string.tv_show)
            BOOK -> context.getString(R.string.book)
            MOVIE -> context.getString(R.string.movie)
            ARTICLE -> context.getString(R.string.article)
            BIOGRAPHY -> context.getString(R.string.biography)
            ANIME -> context.getString(R.string.anime)
        }
    }
}
