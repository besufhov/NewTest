package com.kaankivancdilli.summary.di

import com.kaankivancdilli.summary.data.local.ImageDao
import com.kaankivancdilli.summary.data.local.TextDao
import com.kaankivancdilli.summary.data.repository.SummaryScreenRepository
import com.kaankivancdilli.summary.network.WebSocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide ChatRepository
    @Provides
    @Singleton
    fun provideTextRepository(textDao: TextDao, imageDao: ImageDao): SummaryScreenRepository {
        return SummaryScreenRepository(textDao, imageDao)
    }

    // Provide WebSocket Manager
    @Provides
    @Singleton
    fun provideWebSocketManager(): WebSocketManager = WebSocketManager()
}
