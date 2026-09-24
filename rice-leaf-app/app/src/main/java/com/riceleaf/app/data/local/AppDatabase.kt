package com.riceleaf.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.riceleaf.app.data.local.dao.CacheDao
import com.riceleaf.app.data.local.entity.DiseaseCacheEntity
import com.riceleaf.app.data.local.entity.RecordCacheEntity

@Database(
    entities = [DiseaseCacheEntity::class, RecordCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
