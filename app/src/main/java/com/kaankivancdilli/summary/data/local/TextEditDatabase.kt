package com.kaankivancdilli.summary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaankivancdilli.summary.data.model.local.SaveEditTexts

@Database(entities = [SaveEditTexts::class], version = 1)
abstract class TextEditDatabase : RoomDatabase() {
    abstract fun textEditDao(): TextEditDao
}