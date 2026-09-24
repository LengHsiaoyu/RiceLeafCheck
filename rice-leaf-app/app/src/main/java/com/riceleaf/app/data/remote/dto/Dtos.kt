package com.riceleaf.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Generic API response
data class ApiResponse<T>(
    val code: Int,
    val data: T?,
    val message: String?,
    val timestamp: String?
)

// Disease detection response
data class DetectResponse(
    val recordId: Long,
    val diseaseName: String?,
    val confidence: Double?,
    val lesionAreaRatio: Double?,
    val severityLevel: Int?,
    val symptomDesc: String?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val createTime: String?
)

// Record list
data class RecordListResponse(
    val total: Long,
    val page: Int,
    val size: Int,
    val records: List<RecordItem>
)

data class RecordItem(
    val id: Long,
    val samplePoint: String?,
    val plantNo: String?,
    val leafPosition: String?,
    val diseaseName: String?,
    val severityLevel: Int?,
    val thumbnailUrl: String?,
    val createTime: String?
)

// Record detail
data class RecordDetailResponse(
    val id: Long,
    val samplePoint: String?,
    val plantNo: String?,
    val leafPosition: String?,
    val diseaseName: String?,
    val symptomDesc: String?,
    val confidence: Double?,
    val lesionAreaRatio: Double?,
    val severityLevel: Int?,
    val remark: String?,
    val imageUrl: String?,
    val createTime: String?,
    val updateTime: String?
)

// Update request
data class RecordUpdateRequest(
    val samplePoint: String?,
    val plantNo: String?,
    val leafPosition: String?,
    val diseaseId: Long?,
    val lesionAreaRatio: Double?,
    val severityLevel: Int?,
    val remark: String?
)

// Disease dict
data class DiseaseDict(
    val id: Long,
    val name: String,
    val symptom: String?,
    val spotFeature: String?,
    val sortOrder: Int?
)
