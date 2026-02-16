package com.kaankivancdilli.summary.di.module

import android.content.Context
import androidx.room.Room
import com.kaankivancdilli.summary.data.local.dao.anything.AnythingDao
import com.kaankivancdilli.summary.data.local.dao.image.ImageDao
import com.kaankivancdilli.summary.data.local.database.text.TextDatabase
import com.kaankivancdilli.summary.data.local.dao.textedit.TextEditDao
import com.kaankivancdilli.summary.data.local.dao.text.TextDao
import com.kaankivancdilli.summary.data.local.database.textedit.TextEditDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTextDatabase(@ApplicationContext context: Context): TextDatabase {
        return Room.databaseBuilder(
            context,
            TextDatabase::class.java,
            "text_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTextEditDatabase(@ApplicationContext context: Context): TextEditDatabase {
        return Room.databaseBuilder(
            context,
            TextEditDatabase::class.java,
            "text_edit_database"
        ).build()
    }

    @Provides
    fun provideAnythingDao(database: TextDatabase): AnythingDao {
        return database.anythingDao()
    }

    @Provides
    fun provideTextDao(database: TextDatabase): TextDao {
        return database.textDao()
    }

    @Provides @Singleton
    fun provideImageDao(db: TextDatabase): ImageDao = db.imageDao()

    @Provides
    fun provideTextEditDao(database: TextEditDatabase): TextEditDao {
        return database.textEditDao()
    }
}
