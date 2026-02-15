package com.kaankivancdilli.summary.core.domain.fullanything

import android.content.Context
import com.kaankivancdilli.summary.core.domain.model.TextAction
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType

class FullAnythingEditedResponseUpdater(private val context: Context) {

    data class UpdateResult(
        val updatedSaveAnything: SaveAnything,
        val actionKey: String
    )

    fun update(existing: SaveAnything, action: ActionType, editedText: String): UpdateResult {
        val updated = when (action) {
            ActionType.SUMMARIZE -> existing.copy(summarize = editedText)
            ActionType.PARAPHRASE -> existing.copy(paraphrase = editedText)
            ActionType.REPHRASE -> existing.copy(rephrase = editedText)
            ActionType.EXPAND -> existing.copy(expand = editedText)
            ActionType.BULLETPOINT -> existing.copy(bulletpoint = editedText)
            else -> existing
        }

        val actionKey = TextAction.fromActionType(action, context)?.label ?: "Original"

        return UpdateResult(
            updatedSaveAnything = updated,
            actionKey = actionKey
        )
    }
}