package com.kaankivancdilli.summary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaankivancdilli.summary.data.model.local.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: ImageEntity)

    @Query("SELECT * FROM images WHERE textId = :textId")
    fun getImagesForText(textId: Int): Flow<List<ImageEntity>>

    @Query("DELETE FROM images WHERE textId = :textId")
    suspend fun deleteByTextId(textId: Int)
}