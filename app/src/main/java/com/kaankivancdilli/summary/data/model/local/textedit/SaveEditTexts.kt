package com.kaankivancdilli.summary.data.model.local.textedit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edit_texts")
data class SaveEditTexts(
    @PrimaryKey val id: Int = 1,
    val content: String,
    val isUserMessage: Boolean
)