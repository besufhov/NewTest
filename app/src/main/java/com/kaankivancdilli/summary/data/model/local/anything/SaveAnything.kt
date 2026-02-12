package com.kaankivancdilli.summary.data.model.local.anything

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anything")
data class SaveAnything(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val summarize: String,
    val paraphrase: String,
    val rephrase: String,
    val expand: String,
    val bulletpoint: String,
    val isUserMessage: Boolean,
    val name: String,
    val season: String,
    val episode: String,
    val author: String,
    val chapter: String,
    val director: String,
    val year: String,
    val birthday: String,
    val source: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)