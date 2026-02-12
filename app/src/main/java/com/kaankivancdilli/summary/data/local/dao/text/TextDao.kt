package com.kaankivancdilli.summary.data.local.dao.text

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts
import kotlinx.coroutines.flow.Flow

@Dao
interface TextDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SaveTexts): Long

    @Query("SELECT * FROM texts ORDER BY id ASC")
    fun getAllMessages(): Flow<List<SaveTexts>>

    @Query("SELECT * FROM texts WHERE id = :textId LIMIT 1")
    fun getSavedTextByIdFlow(textId: Int): Flow<SaveTexts?>

    @Query("UPDATE texts SET summarize = :summarize WHERE ocrText = :ocrText")
    suspend fun updateSummary(ocrText: String, summarize: String)

    @Query("UPDATE texts SET paraphrase = :paraphrase WHERE ocrText = :ocrText")
    suspend fun updateParaphrase(ocrText: String, paraphrase: String) // Make sure suspend is here ✅

    @Query("UPDATE texts SET rephrase = :rephrase WHERE ocrText = :ocrText")
    suspend fun updateRephrase(ocrText: String, rephrase: String) // Make sure suspend is here ✅

    @Query("UPDATE texts SET expand = :expand WHERE ocrText = :ocrText")
    suspend fun updateExpand(ocrText: String, expand: String) // Make sure suspend is here ✅

    @Query("UPDATE texts SET bulletpoint = :bulletpoint WHERE ocrText = :ocrText")
    suspend fun updateBulletpoint(ocrText: String, bulletpoint: String) // Make sure suspend is here ✅

    @Delete
    suspend fun deleteMessage(message: SaveTexts)

    @Query("SELECT * FROM texts WHERE ocrText = :ocrText ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntryForOcr(ocrText: String): SaveTexts?

    @Query("SELECT * FROM texts WHERE id = :textId LIMIT 1")
    suspend fun getSavedTextById(textId: Int): SaveTexts?

    @Query("SELECT * FROM texts WHERE ocrText = :ocrText LIMIT 1")
    fun getSavedTextByOcrText(ocrText: String): SaveTexts?

}






