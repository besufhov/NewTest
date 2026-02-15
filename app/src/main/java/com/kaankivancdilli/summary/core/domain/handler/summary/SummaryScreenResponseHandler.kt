package com.kaankivancdilli.summary.core.domain.handler.summary

import android.graphics.Bitmap
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import com.kaankivancdilli.summary.core.domain.summary.SaveTextsHandler
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SummaryScreenResponseHandler @Inject constructor(
    private val saveTextsHandler: SaveTextsHandler,
    private val subscriptionChecker: SubscriptionChecker
) {

    suspend fun handleSuccess(
        saveTextsFlow: MutableStateFlow<List<SaveTexts>>,
        isSummarizedText: MutableStateFlow<Boolean>,
        isParaphrasedText: MutableStateFlow<Boolean>,
        isRephrasedText: MutableStateFlow<Boolean>,
        isExpandedText: MutableStateFlow<Boolean>,
        isBulletpointedText: MutableStateFlow<Boolean>,
        extractedText: String,
        ocrText: String,
        imageTriples: List<Triple<String, Bitmap, String>>?,
        scope: CoroutineScope,
        onLoadSavedText: (Int) -> Unit = {},
        triggerInterstitialAd: () -> Unit = {}
    ) {
        val existing = saveTextsFlow.value.firstOrNull()
        val updated = existing?.copy(
            summarize = if (isSummarizedText.value) extractedText else existing.summarize,
            paraphrase = if (isParaphrasedText.value) extractedText else existing.paraphrase,
            rephrase = if (isRephrasedText.value) extractedText else existing.rephrase,
            expand = if (isExpandedText.value) extractedText else existing.expand,
            bulletpoint = if (isBulletpointedText.value) extractedText else existing.bulletpoint
        ) ?: SaveTexts(
            summarize = if (isSummarizedText.value) extractedText else "",
            paraphrase = if (isParaphrasedText.value) extractedText else "",
            rephrase = if (isRephrasedText.value) extractedText else "",
            expand = if (isExpandedText.value) extractedText else "",
            bulletpoint = if (isBulletpointedText.value) extractedText else "",
            ocrText = ocrText,
            isUserMessage = false
        )

        saveTextsFlow.value = listOf(updated)

        saveTextsHandler.saveTexts(
            scope = scope,
            id = updated.id.takeIf { it > 0 },
            summarize = updated.summarize,
            ocrText = updated.ocrText,
            rephrase = updated.rephrase,
            paraphrase = updated.paraphrase,
            expand = updated.expand,
            bulletpoint = updated.bulletpoint,
            isUserMessage = false,
            imageTriples = imageTriples,
            onLoadSavedText = onLoadSavedText
        )

        isSummarizedText.value = false
        isParaphrasedText.value = false
        isRephrasedText.value = false
        isExpandedText.value = false
        isBulletpointedText.value = false

        if (!subscriptionChecker.isUserSubscribed() && isSummarizedText.value.not()) {
            val usageCount = saveTextsFlow.value.size
            if (usageCount > 0 && usageCount % 6 == 0) {
                triggerInterstitialAd()
            }
        }
    }
}