package com.kaankivancdilli.summary.di.module.database

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
        ).fallbackToDestructiveMigration() // THIS ALLOWS RESETTING THE DATABASE put .fallback here
            .build()
    }
//.fallbackToDestructiveMigration()
    @Provides
    @Singleton
    fun provideTextEditDatabase(@ApplicationContext context: Context): TextEditDatabase {
        return Room.databaseBuilder(
            context,
            TextEditDatabase::class.java,
            "text_edit_database" // This is for edit_texts
        ).build()
    }

    //@Provides
    //@Singleton
    //fun provideAnythingDatabase(@ApplicationContext context: Context): AnythingDatabase {
     //   return Room.databaseBuilder(
     //       context,
      //      AnythingDatabase::class.java,
     //       "anything_database" // This is for edit_texts
     //   ).build()
    //}
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

    //@Provides
    //fun provideAnythingDao(database: AnythingDatabase): AnythingDao {
    //    return database.anythingDao()
    //}


}
