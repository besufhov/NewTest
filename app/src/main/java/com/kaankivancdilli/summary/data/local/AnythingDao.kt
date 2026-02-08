package com.kaankivancdilli.summary.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.data.model.local.SaveTexts
import kotlinx.coroutines.flow.Flow

@Dao
interface AnythingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnything(anything: SaveAnything)

    @Query("SELECT * FROM anything ORDER BY timestamp DESC")
   // @Query("SELECT * FROM anything ORDER BY id ASC")
    fun getAllAnything(): Flow<List<SaveAnything>>

    @Query("UPDATE anything SET summarize = :summarize WHERE summarize = :summarize")
    suspend fun updateSummary(summarize: String)

    @Query("UPDATE anything SET paraphrase = :paraphrase WHERE summarize = :summarize")
    suspend fun updateParaphrase(summarize: String, paraphrase: String) // Make sure suspend is here ✅

    @Query("UPDATE anything SET rephrase = :rephrase WHERE summarize = :summarize")
    suspend fun updateRephrase(summarize: String, rephrase: String) // Make sure suspend is here ✅

    @Query("UPDATE anything SET expand = :expand WHERE summarize = :summarize")
    suspend fun updateExpand(summarize: String, expand: String) // Make sure suspend is here ✅

    @Query("UPDATE anything SET bulletpoint = :bulletpoint WHERE summarize = :summarize")
    suspend fun updateBulletpoint(summarize: String, bulletpoint: String) // Make sure suspend is here ✅

    @Delete
    suspend fun deleteAnything(user: SaveAnything)

    @Query("SELECT * FROM anything WHERE summarize = :summarize ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntryForSummary(summarize: String): SaveAnything?

    @Query("SELECT * FROM anything WHERE id = :textId LIMIT 1")
    suspend fun getAnythingTextById(textId: Int): SaveAnything?

    @Query("SELECT * FROM anything WHERE summarize = :summarize LIMIT 1")
    fun getAnythingTextByOcrText(summarize: String): SaveAnything?
}