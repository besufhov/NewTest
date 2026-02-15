package com.kaankivancdilli.summary.core.domain.model

import android.content.Context
import com.kaankivancdilli.summary.R
import com.kaankivancdilli.summary.ui.screens.sub.summary.type.ActionType

sealed class TextAction(val label: String) {

    class Summarize(context: Context) : TextAction(context.getString(R.string.summarize))
    class Paraphrase(context: Context) : TextAction(context.getString(R.string.paraphrase))
    class Rephrase(context: Context) : TextAction(context.getString(R.string.rephrase))
    class Expand(context: Context) : TextAction(context.getString(R.string.expand))
    class Bulletpoint(context: Context) : TextAction(context.getString(R.string.bullet_point))

    companion object {
        fun fromActionType(action: ActionType, context: Context): TextAction? = when(action) {
            ActionType.SUMMARIZE -> Summarize(context)
            ActionType.PARAPHRASE -> Paraphrase(context)
            ActionType.REPHRASE -> Rephrase(context)
            ActionType.EXPAND -> Expand(context)
            ActionType.BULLETPOINT -> Bulletpoint(context)
            else -> null
        }
    }
}