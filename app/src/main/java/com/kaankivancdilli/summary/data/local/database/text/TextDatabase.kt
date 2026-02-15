package com.kaankivancdilli.summary.data.local.database.text

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kaankivancdilli.summary.data.local.dao.anything.AnythingDao
import com.kaankivancdilli.summary.data.local.dao.image.ImageDao
import com.kaankivancdilli.summary.data.local.dao.text.TextDao
import com.kaankivancdilli.summary.data.model.local.converter.Converters
import com.kaankivancdilli.summary.data.model.local.image.ImageEntity
import com.kaankivancdilli.summary.data.model.local.SaveAnything
import com.kaankivancdilli.summary.data.model.local.text.SaveTexts

@Database(entities = [SaveTexts::class, SaveAnything::class, ImageEntity::class], version = 8)
@TypeConverters(Converters::class)
abstract class TextDatabase : RoomDatabase() {
    abstract fun textDao(): TextDao
    abstract fun anythingDao(): AnythingDao
    abstract fun imageDao(): ImageDao
}