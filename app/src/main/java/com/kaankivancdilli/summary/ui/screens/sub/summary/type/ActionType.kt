package com.kaankivancdilli.summary.ui.screens.sub.summary.type

enum class ActionType {
    SUMMARIZE, PARAPHRASE, REPHRASE, EXPAND, BULLETPOINT, ORIGINAL;

    companion object {
        fun from(value: String?): ActionType? {
            return try {
                value?.let { ActionType.valueOf(it) }
            } catch (e: Exception) {
                null
            }
        }
    }
}
