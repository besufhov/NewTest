package com.kaankivancdilli.summary.data.model.local.text

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "texts")
data class SaveTexts(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val summarize: String,
    val paraphrase: String,
    val rephrase: String,
    val expand: String,
    val bulletpoint: String,
    val ocrText: String,
    val isUserMessage: Boolean,
 //   val imageData: ByteArray?,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)



