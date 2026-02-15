package com.kaankivancdilli.summary.core.mapper.fullanything

import android.content.Context
import com.kaankivancdilli.summary.core.domain.model.TextAction
import com.kaankivancdilli.summary.data.model.local.SaveAnything

class SavedAnythingMapper(private val context: Context) {

    data class ResultsBundle(
        val results: Map<String, String>,
        val completed: Set<String>,
        val originalSummarizedText: String
    )

    fun map(saved: SaveAnything): ResultsBundle {
        val results = mutableMapOf<String, String>()
        val completed = mutableSetOf<String>()

        val actions = listOf(
            TextAction.Summarize(context),
            TextAction.Paraphrase(context),
            TextAction.Rephrase(context),
            TextAction.Expand(context),
            TextAction.Bulletpoint(context)
        )

        actions.forEach { action ->
            val text = when (action) {
                is TextAction.Summarize -> saved.summarize
                is TextAction.Paraphrase -> saved.paraphrase
                is TextAction.Rephrase -> saved.rephrase
                is TextAction.Expand -> saved.expand
                is TextAction.Bulletpoint -> saved.bulletpoint
            }

            if (text.isNotBlank()) {
                results[action.label] = text
                completed += action.label
            }
        }

        return ResultsBundle(
            results = results,
            completed = completed,
            originalSummarizedText = saved.summarize
        )
    }
}
