package com.riceleaf.local.di

import android.content.Context
import com.riceleaf.local.data.local.AppDatabase
import com.riceleaf.local.data.local.dao.RecordDao
import com.riceleaf.local.data.repository.PhotoRepository
import com.riceleaf.local.ml.InferenceEngine
import com.riceleaf.local.util.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, "riceleaf_local.db")
            .build()

    @Provides
    fun provideRecordDao(db: AppDatabase): RecordDao = db.recordDao()

    @Provides
    @Singleton
    fun providePhotoRepository(
        db: AppDatabase,
        @ApplicationContext context: Context
    ): PhotoRepository = PhotoRepository(db, context)

    @Provides
    @Singleton
    fun provideInferenceEngine(
        @ApplicationContext context: Context,
        settingsManager: SettingsManager
    ): InferenceEngine = InferenceEngine(context, settingsManager)

    @Provides
    @Singleton
    fun provideSettingsManager(
        @ApplicationContext context: Context
    ): SettingsManager = SettingsManager(context)
}
