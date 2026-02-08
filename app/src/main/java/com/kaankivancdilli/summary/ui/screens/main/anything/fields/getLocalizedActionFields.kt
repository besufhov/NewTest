package com.kaankivancdilli.summary.ui.screens.main.anything.fields

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kaankivancdilli.summary.R

@Composable
fun getLocalizedActionFields(): Map<String, List<Pair<String, String>>> {
    return mapOf(
        stringResource(R.string.tv_show) to listOf(
            "name" to stringResource(R.string.name),
            "season" to stringResource(R.string.season),
            "episode" to stringResource(R.string.episode)
        ),
        stringResource(R.string.book) to listOf(
            "name" to stringResource(R.string.name),
            "author" to stringResource(R.string.author),
            "chapter" to stringResource(R.string.chapter)
        ),
        stringResource(R.string.movie) to listOf(
            "name" to stringResource(R.string.name),
            "director" to stringResource(R.string.director),
            "year" to stringResource(R.string.year)
        ),
        stringResource(R.string.article) to listOf(
            "name" to stringResource(R.string.name),
            "author" to stringResource(R.string.author),
            "source" to stringResource(R.string.source)
        ),
        stringResource(R.string.biography) to listOf(
            "name" to stringResource(R.string.name),
            "birthday" to stringResource(R.string.birthday)
        ),
        stringResource(R.string.anime) to listOf(
            "name" to stringResource(R.string.name),
            "season" to stringResource(R.string.season),
            "episode" to stringResource(R.string.episode)
        )
    )
}