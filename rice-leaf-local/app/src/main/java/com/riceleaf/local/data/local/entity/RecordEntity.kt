package com.riceleaf.local.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val plantNo: String,
    val leafPosition: String,
    val diseaseName: String? = null,
    val confidence: Float? = null,
    val lesionAreaRatio: Float? = null,
    val severityLevel: Int? = null,
    val symptomDesc: String? = null,
    val remark: String = "",
    val status: Int = 0,
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis()
)
