package com.kaankivancdilli.summary.core.domain.summary

import android.content.Context
import android.util.Log
import com.kaankivancdilli.summary.core.domain.model.TextAction
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import com.kaankivancdilli.summary.data.repository.sub.SummaryScreenRepository
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SummaryEditedResponseUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SummaryScreenRepository
) {

    fun updateEditedResponse(
        scope: CoroutineScope,
        action: ActionType,
        editedText: String,
        originalId: Int?,
        onResultKey: (String, String) -> Unit,
        onUpdate: (SaveTexts) -> Unit
    ) {
        if (originalId == null || originalId == 0) return

        scope.launch {
            val existing = repository.getSavedTextById(originalId)
            if (existing == null) {
                Log.e("EditedResponseHandler", "No matching SaveTexts entry found for ID: $originalId")
                return@launch
            }

            val updated = when (action) {
                ActionType.SUMMARIZE -> existing.copy(summarize = editedText)
                ActionType.PARAPHRASE -> existing.copy(paraphrase = editedText)
                ActionType.REPHRASE -> existing.copy(rephrase = editedText)
                ActionType.EXPAND -> existing.copy(expand = editedText)
                ActionType.BULLETPOINT -> existing.copy(bulletpoint = editedText)
                ActionType.ORIGINAL -> existing.copy(ocrText = editedText)
            }

            val actionKey = TextAction.fromActionType(action, context)?.label ?: "Original"

            onResultKey(actionKey, editedText)
            repository.saveText(updated)
            onUpdate(updated)

            Log.d("EditedResponseHandler", "Saved updated result for $action: $editedText")
        }
    }
}