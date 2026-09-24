package com.riceleaf.app.data.local.dao

import androidx.room.*
import com.riceleaf.app.data.local.entity.DiseaseCacheEntity
import com.riceleaf.app.data.local.entity.RecordCacheEntity

@Dao
interface CacheDao {

    // Disease cache
    @Query("SELECT * FROM disease_cache ORDER BY sortOrder ASC")
    suspend fun getAllDiseases(): List<DiseaseCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiseases(diseases: List<DiseaseCacheEntity>)

    @Query("DELETE FROM disease_cache")
    suspend fun clearDiseases()

    // Record cache
    @Query("SELECT * FROM record_cache ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int = 50): List<RecordCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordCacheEntity>)

    @Query("DELETE FROM record_cache")
    suspend fun clearRecords()

    @Transaction
    suspend fun clearAll() {
        clearDiseases()
        clearRecords()
    }
}
