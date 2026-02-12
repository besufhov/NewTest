package com.kaankivancdilli.summary.data.model.local.image

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts

@Entity(
    tableName = "images",
    foreignKeys = [ ForeignKey(
        entity = SaveTexts::class,
        parentColumns = ["id"],
        childColumns  = ["textId"],
        onDelete      = CASCADE
    ) ],
    indices = [ Index("textId") ]
)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val textId: Int,
    val name: String,
    val recognizedText: String,
    val imageData: ByteArray
)