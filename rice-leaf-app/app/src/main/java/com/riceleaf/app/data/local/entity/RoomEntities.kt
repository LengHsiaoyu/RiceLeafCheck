package com.riceleaf.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disease_cache")
data class DiseaseCacheEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val symptom: String?,
    val spotFeature: String?,
    val sortOrder: Int?
)

@Entity(tableName = "record_cache")
data class RecordCacheEntity(
    @PrimaryKey val id: Long,
    val samplePoint: String?,
    val plantNo: String?,
    val leafPosition: String?,
    val diseaseName: String?,
    val severityLevel: Int?,
    val thumbnailUrl: String?,
    val createTime: String?
)
