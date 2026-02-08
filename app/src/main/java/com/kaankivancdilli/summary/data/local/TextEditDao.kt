package com.kaankivancdilli.summary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaankivancdilli.summary.data.model.local.SaveEditTexts
import kotlinx.coroutines.flow.Flow

@Dao
interface TextEditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveText(text: SaveEditTexts) // Always replaces the single entry

    @Query("SELECT * FROM edit_texts WHERE id = 1 LIMIT 1")
    fun getSavedText(): Flow<SaveEditTexts?>

    @Query("DELETE FROM edit_texts")
    suspend fun clearText()
}