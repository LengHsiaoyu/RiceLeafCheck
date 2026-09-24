package com.riceleaf.app.network

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val code: Int,
    val data: T?,
    val message: String?,
    val timestamp: String?
)

data class DiseaseDict(
    val id: Long,
    val name: String,
    val symptom: String?,
    @SerializedName("spotFeature")
    val spotFeature: String?,
    @SerializedName("sortOrder")
    val sortOrder: Int?
)

data class DetectRequest(
    @SerializedName("samplePoint")
    val samplePoint: String,
    @SerializedName("plantNo")
    val plantNo: String,
    @SerializedName("leafPosition")
    val leafPosition: String
)

data class DetectResponse(
    @SerializedName("recordId")
    val recordId: Long,
    @SerializedName("diseaseName")
    val diseaseName: String,
    val confidence: Double,
    @SerializedName("lesionAreaRatio")
    val lesionAreaRatio: Double,
    @SerializedName("severityLevel")
    val severityLevel: Int,
    @SerializedName("symptomDesc")
    val symptomDesc: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("createTime")
    val createTime: String?
)

data class RecordItem(
    val id: Long,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,
    @SerializedName("diseaseName")
    val diseaseName: String?,
    @SerializedName("severityLevel")
    val severityLevel: Int?,
    @SerializedName("createTime")
    val createTime: String?,
    @SerializedName("samplePoint")
    val samplePoint: String?,
    @SerializedName("plantNo")
    val plantNo: String?,
    @SerializedName("leafPosition")
    val leafPosition: String?,
    @SerializedName("confidence")
    val confidence: Double?,
    @SerializedName("lesionAreaRatio")
    val lesionAreaRatio: Double?,
    @SerializedName("symptomDesc")
    val symptomDesc: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    val remark: String?
)

data class RecordListResponse(
    val total: Long,
    val page: Int,
    val records: List<RecordItem>
)
