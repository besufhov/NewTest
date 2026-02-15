package com.kaankivancdilli.summary.core.domain.summary

import android.graphics.Bitmap
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.data.repository.sub.SummaryScreenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SaveTextsHandler @Inject constructor(
    private val repository: SummaryScreenRepository
) {

    fun saveTexts(
        scope: CoroutineScope,
        id: Int?,
        summarize: String,
        ocrText: String,
        rephrase: String,
        paraphrase: String,
        expand: String,
        bulletpoint: String,
        isUserMessage: Boolean,
        imageTriples: List<Triple<String, Bitmap, String>>?,
        onLoadSavedText: (Int) -> Unit
    ) {
        val triples = imageTriples ?: emptyList()

        scope.launch {
            if (id == null || id == 0) {

                val newText = SaveTexts(
                    summarize = summarize,
                    paraphrase = paraphrase,
                    rephrase = rephrase,
                    expand = expand,
                    bulletpoint = bulletpoint,
                    ocrText = ocrText,
                    isUserMessage = isUserMessage
                )

                val newId = repository
                    .saveTextWithImages(newText, triples)
                    .toInt()

                onLoadSavedText(newId)
            } else {

                val existing = repository.getSavedTextById(id) ?: return@launch

                val updated = existing.copy(
                    summarize = summarize.ifBlank { existing.summarize },
                    paraphrase = paraphrase.ifBlank { existing.paraphrase },
                    rephrase = rephrase.ifBlank { existing.rephrase },
                    expand = expand.ifBlank { existing.expand },
                    bulletpoint = bulletpoint.ifBlank { existing.bulletpoint }
                )

                repository.saveText(updated)

                if (triples.isNotEmpty()) {
                    repository.saveTextWithImages(updated, triples)
                }

                onLoadSavedText(id)
            }
        }
    }
}