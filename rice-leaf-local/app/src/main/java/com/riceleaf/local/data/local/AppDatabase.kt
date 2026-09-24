package com.riceleaf.local.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.riceleaf.local.data.local.dao.RecordDao
import com.riceleaf.local.data.local.entity.RecordEntity

@Database(entities = [RecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
}
