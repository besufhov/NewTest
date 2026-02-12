package com.kaankivancdilli.summary.data.local.database.textedit

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kaankivancdilli.summary.data.local.dao.textedit.TextEditDao
import com.kaankivancdilli.summary.data.model.local.textedit.SaveEditTexts

@Database(entities = [SaveEditTexts::class], version = 1)
abstract class TextEditDatabase : RoomDatabase() {
    abstract fun textEditDao(): TextEditDao
}