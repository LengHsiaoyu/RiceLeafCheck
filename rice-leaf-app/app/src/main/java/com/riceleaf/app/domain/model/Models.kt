package com.riceleaf.app.domain.model

data class DiseaseInfo(
    val id: Long,
    val name: String,
    val symptom: String,
    val spotFeature: String
)

data class DetectionResult(
    val recordId: Long,
    val diseaseName: String,
    val confidence: Double,
    val lesionAreaRatio: Double,
    val severityLevel: Int,
    val symptomDesc: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val createTime: String
)

data class RecordSummary(
    val id: Long,
    val samplePoint: String,
    val plantNo: String,
    val leafPosition: String,
    val diseaseName: String,
    val severityLevel: Int,
    val thumbnailUrl: String,
    val createTime: String
)

data class RecordDetail(
    val id: Long,
    val samplePoint: String,
    val plantNo: String,
    val leafPosition: String,
    val diseaseName: String,
    val symptomDesc: String,
    val confidence: Double,
    val lesionAreaRatio: Double,
    val severityLevel: Int,
    val remark: String,
    val imageUrl: String,
    val createTime: String,
    val updateTime: String
)
