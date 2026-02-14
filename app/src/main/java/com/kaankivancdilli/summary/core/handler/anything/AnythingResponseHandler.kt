package com.kaankivancdilli.summary.core.handler.anything

import android.content.Context
import android.util.Log
import com.kaankivancdilli.summary.core.domain.SubscriptionChecker
import com.kaankivancdilli.summary.core.state.FreeUsageTracker
import com.kaankivancdilli.summary.data.model.local.anything.SaveAnything
import com.kaankivancdilli.summary.data.repository.main.anything.AnythingScreenRepository
import com.kaankivancdilli.summary.core.mapper.anything.SaveAnythingMapper
import com.kaankivancdilli.summary.core.mapper.anything.TypeMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AnythingResponseHandler @Inject constructor(
    private val freeUsageTracker: FreeUsageTracker,
    private val subscriptionChecker: SubscriptionChecker,
    private val repository: AnythingScreenRepository,
    @ApplicationContext private val context: Context
) {

    fun handleSuccess(
        extractedText: String,
        selectedActionType: String?,
        latestInputFields: Map<String, String>,
        isSummarized: Boolean,
        onMessageBuilt: (SaveAnything) -> Unit,
        onSummarizedStateReset: () -> Unit,
        onTriggerInterstitial: () -> Unit
    ) {
        val actionType = selectedActionType ?: "unknown"
        val englishKey = TypeMapper.toEnglishKey(context, actionType)
        val fields = latestInputFields

        logFields(fields)

        val message = buildSaveAnythingMessage(englishKey, extractedText, fields)

        if (isSummarized) {
            processSummarizedResponse(
                extractedText,
                englishKey,
                fields,
                message,
                onMessageBuilt,
                onSummarizedStateReset,
                onTriggerInterstitial
            )
        } else {
            onMessageBuilt(message)
        }

        Log.d("AnythingViewModel", "Updated saveAnything list: $message")
    }

    fun handleError(setError: (String?) -> Unit, message: String?) {
        setError(message)
    }

    private fun logFields(fields: Map<String, String>) {
        Log.d(
            "SaveAnything",
            "Saving fields: ${fields["name"]}, ${fields["season"]}, ${fields["episode"]}, " +
                    "${fields["author"]}, ${fields["chapter"]}, ${fields["director"]}, " +
                    "${fields["year"]}, ${fields["source"]}, ${fields["birthday"]}"
        )
    }

    private fun buildSaveAnythingMessage(
        englishKey: String,
        extractedText: String,
        fields: Map<String, String>
    ): SaveAnything {
        return SaveAnything(
            type = englishKey,
            summarize = extractedText,
            paraphrase = if (englishKey == "paraphrase") extractedText else "",
            rephrase = if (englishKey == "rephrase") extractedText else "",
            expand = if (englishKey == "expand") extractedText else "",
            bulletpoint = if (englishKey == "bulletpoint") extractedText else "",
            name = fields["name"] ?: "",
            season = fields["season"] ?: "",
            episode = fields["episode"] ?: "",
            author = fields["author"] ?: "",
            chapter = fields["chapter"] ?: "",
            director = fields["director"] ?: "",
            year = fields["year"] ?: "",
            source = fields["source"] ?: "",
            birthday = fields["birthday"] ?: "",
            isUserMessage = false
        )
    }

    private fun processSummarizedResponse(
        extractedText: String,
        englishKey: String,
        fields: Map<String, String>,
        message: SaveAnything,
        onMessageBuilt: (SaveAnything) -> Unit,
        onSummarizedStateReset: () -> Unit,
        onTriggerInterstitial: () -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {

            val usageCount = freeUsageTracker.getCount()

            if (usageCount <= 20) {
                freeUsageTracker.incrementAndGet()
                Log.d("AnythingVM", "Free usage incremented to ${usageCount + 1}")
            }

            repository.saveAnything(
                SaveAnythingMapper.create(extractedText, englishKey, false, fields)
            )

            onMessageBuilt(message)

            onSummarizedStateReset()

            val isSubbed = subscriptionChecker.isUserSubscribed()

            if (!isSubbed && usageCount > 0 && usageCount % 6 == 0) {
                onTriggerInterstitial()
            }
        }
    }
}