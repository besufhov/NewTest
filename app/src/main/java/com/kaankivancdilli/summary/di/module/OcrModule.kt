package com.kaankivancdilli.summary.di.module

import android.content.Context
import com.kaankivancdilli.summary.data.repository.main.photomain.OcrRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    @Provides
    fun provideOcrRepository(
        @ApplicationContext context: Context
    ): OcrRepository = OcrRepository(context)

}