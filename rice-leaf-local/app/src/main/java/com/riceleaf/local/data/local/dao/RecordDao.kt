package com.riceleaf.local.data.local.dao

import androidx.room.*
import com.riceleaf.local.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM records WHERE status = 0 ORDER BY createTime DESC")
    fun getAll(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Long): RecordEntity?

    @Query("SELECT COUNT(*) FROM records WHERE status = 0")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM records WHERE status = 0 ORDER BY createTime DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaged(limit: Int, offset: Int): List<RecordEntity>

    @Query("SELECT * FROM records WHERE status = 0 ORDER BY createTime DESC")
    suspend fun getAllForExport(): List<RecordEntity>
}
