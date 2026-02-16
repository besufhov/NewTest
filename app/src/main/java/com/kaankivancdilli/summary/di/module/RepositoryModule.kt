package com.kaankivancdilli.summary.di.module

import com.kaankivancdilli.summary.data.local.dao.image.ImageDao
import com.kaankivancdilli.summary.data.local.dao.text.TextDao
import com.kaankivancdilli.summary.data.repository.sub.SummaryScreenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTextRepository(textDao: TextDao, imageDao: ImageDao): SummaryScreenRepository {
        return SummaryScreenRepository(textDao, imageDao)
    }


}
