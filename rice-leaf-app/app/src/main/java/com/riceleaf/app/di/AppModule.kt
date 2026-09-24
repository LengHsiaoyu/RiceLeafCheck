package com.riceleaf.app.di

import android.content.Context
import androidx.room.Room
import com.riceleaf.app.data.local.AppDatabase
import com.riceleaf.app.data.local.dao.CacheDao
import com.riceleaf.app.data.remote.ApiService
import com.riceleaf.app.data.remote.RetrofitClient
import com.riceleaf.app.data.repository.RecordRepository
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
    fun provideApiService(): ApiService = RetrofitClient.apiService

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "riceleaf_cache.db"
        ).build()
    }

    @Provides
    fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()

    @Provides
    @Singleton
    fun provideRepository(
        @ApplicationContext context: Context,
        db: AppDatabase
    ): RecordRepository {
        return RecordRepository(context, db)
    }
}
