package com.kaankivancdilli.summary.core.domain.handler.fullanything

import android.content.Context
import com.kaankivancdilli.summary.core.domain.fullanything.FullAnythingSaveHandler
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class FullAnythingResponseHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saveHandler: FullAnythingSaveHandler
) {

    suspend fun handleSuccess(
        saveAnythingFlow: MutableStateFlow<List<SaveAnything>>,
        isSummarizedText: MutableStateFlow<Boolean>,
        isParaphrasedText: MutableStateFlow<Boolean>,
        isRephrasedText: MutableStateFlow<Boolean>,
        isExpandedText: MutableStateFlow<Boolean>,
        isBulletpointedText: MutableStateFlow<Boolean>,
        extractedText: String
    ) {
        val existing = saveAnythingFlow.value.firstOrNull()
        val updated = existing?.let {
            updateSaveAnythingWithExtractedText(
                it,
                extractedText,
                isSummarizedText.value,
                isParaphrasedText.value,
                isRephrasedText.value,
                isExpandedText.value,
                isBulletpointedText.value
            )
        }

        updated?.let {
            saveAnythingFlow.value = listOf(it)
            persistUpdatedSaveAnything(it)
        }

        resetActionFlags(
            isSummarizedText,
            isParaphrasedText,
            isRephrasedText,
            isExpandedText,
            isBulletpointedText
        )
    }

    private fun updateSaveAnythingWithExtractedText(
        existing: SaveAnything,
        text: String,
        summarized: Boolean,
        paraphrased: Boolean,
        rephrased: Boolean,
        expanded: Boolean,
        bulletpointed: Boolean
    ): SaveAnything {
        return existing.copy(
            summarize = if (summarized) text else existing.summarize,
            paraphrase = if (paraphrased) text else existing.paraphrase,
            rephrase = if (rephrased) text else existing.rephrase,
            expand = if (expanded) text else existing.expand,
            bulletpoint = if (bulletpointed) text else existing.bulletpoint
        )
    }

    private suspend fun persistUpdatedSaveAnything(updated: SaveAnything) {
        saveHandler.saveAnything(
            id = updated.id.takeIf { it > 0 },
            summarize = updated.summarize,
            paraphrase = updated.paraphrase,
            rephrase = updated.rephrase,
            expand = updated.expand,
            bulletpoint = updated.bulletpoint,
            isUserMessage = false
        )
    }

    private fun resetActionFlags(
        summarized: MutableStateFlow<Boolean>,
        paraphrased: MutableStateFlow<Boolean>,
        rephrased: MutableStateFlow<Boolean>,
        expanded: MutableStateFlow<Boolean>,
        bulletpointed: MutableStateFlow<Boolean>
    ) {
        summarized.value = false
        paraphrased.value = false
        rephrased.value = false
        expanded.value = false
        bulletpointed.value = false
    }
}