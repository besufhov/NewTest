package com.kaankivancdilli.summary.core.mapper.summary

import android.content.Context
import com.kaankivancdilli.summary.core.domain.model.TextAction
import com.kaankivancdilli.summary.data.model.local.image.ImageEntity
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.data.repository.sub.SummaryScreenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SavedTextMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SummaryScreenRepository
) {

    suspend fun load(
        textId: Int,
        onResult: (saveText: SaveTexts?, images: List<ImageEntity>) -> Unit,
        onUpdateResults: (results: Map<String, String>, completed: Set<String>) -> Unit
    ) {
        repository.getTextWithImages(textId).collect { (textRow, images) ->
            if (textRow == null) {
                onResult(null, images)
                return@collect
            }

            onResult(textRow, images)

            val initialResults = mutableMapOf<String, String>()
            val initialCompleted = mutableSetOf<String>()

            val actions = listOf(
                TextAction.Summarize(context),
                TextAction.Paraphrase(context),
                TextAction.Rephrase(context),
                TextAction.Expand(context),
                TextAction.Bulletpoint(context)
            )

            actions.forEach { action ->
                val text = when (action) {
                    is TextAction.Summarize -> textRow.summarize
                    is TextAction.Paraphrase -> textRow.paraphrase
                    is TextAction.Rephrase -> textRow.rephrase
                    is TextAction.Expand -> textRow.expand
                    is TextAction.Bulletpoint -> textRow.bulletpoint
                }

                if (text.isNotBlank()) {
                    initialResults[action.label] = text
                    initialCompleted += action.label
                }
            }

            onUpdateResults(initialResults, initialCompleted)
        }
    }
}