package com.riceleaf.app.data.repository

import android.content.Context
import android.net.Uri
import com.riceleaf.app.data.local.AppDatabase
import com.riceleaf.app.data.local.entity.DiseaseCacheEntity
import com.riceleaf.app.data.local.entity.RecordCacheEntity
import com.riceleaf.app.data.remote.RetrofitClient
import com.riceleaf.app.data.remote.dto.RecordUpdateRequest
import com.riceleaf.app.domain.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class RecordRepository(
    private val context: Context,
    private val db: AppDatabase
) {

    private val api = RetrofitClient.apiService

    suspend fun detect(
        imageUri: Uri,
        samplePoint: String,
        plantNo: String,
        leafPosition: String
    ): Result<DetectionResult> {
        return try {
            val file = uriToFile(imageUri)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)
            val spBody = samplePoint.toRequestBody("text/plain".toMediaTypeOrNull())
            val pnBody = plantNo.toRequestBody("text/plain".toMediaTypeOrNull())
            val lpBody = leafPosition.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.detect(imagePart, spBody, pnBody, lpBody)
            if (response.code == 200 && response.data != null) {
                val d = response.data
                Result.success(DetectionResult(
                    recordId = d.recordId,
                    diseaseName = d.diseaseName ?: "未知",
                    confidence = d.confidence ?: 0.0,
                    lesionAreaRatio = d.lesionAreaRatio ?: 0.0,
                    severityLevel = d.severityLevel ?: 0,
                    symptomDesc = d.symptomDesc ?: "",
                    imageUrl = d.imageUrl ?: "",
                    thumbnailUrl = d.thumbnailUrl ?: "",
                    createTime = d.createTime ?: ""
                ))
            } else {
                Result.failure(Exception(response.message ?: "识别失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecords(
        page: Int = 1,
        size: Int = 20
    ): Result<Pair<List<RecordSummary>, Long>> {
        return try {
            val response = api.getRecords(page = page, size = size)
            if (response.code == 200 && response.data != null) {
                val summaries = response.data.records.map { item ->
                    RecordSummary(
                        id = item.id,
                        samplePoint = item.samplePoint ?: "",
                        plantNo = item.plantNo ?: "",
                        leafPosition = item.leafPosition ?: "",
                        diseaseName = item.diseaseName ?: "未知",
                        severityLevel = item.severityLevel ?: 0,
                        thumbnailUrl = item.thumbnailUrl ?: "",
                        createTime = item.createTime ?: ""
                    )
                }
                Result.success(Pair(summaries, response.data.total))
            } else {
                Result.failure(Exception(response.message ?: "加载失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecordDetail(id: Long): Result<RecordDetail> {
        return try {
            val response = api.getRecordDetail(id)
            if (response.code == 200 && response.data != null) {
                val d = response.data
                Result.success(RecordDetail(
                    id = d.id,
                    samplePoint = d.samplePoint ?: "",
                    plantNo = d.plantNo ?: "",
                    leafPosition = d.leafPosition ?: "",
                    diseaseName = d.diseaseName ?: "未知",
                    symptomDesc = d.symptomDesc ?: "",
                    confidence = d.confidence ?: 0.0,
                    lesionAreaRatio = d.lesionAreaRatio ?: 0.0,
                    severityLevel = d.severityLevel ?: 0,
                    remark = d.remark ?: "",
                    imageUrl = d.imageUrl ?: "",
                    createTime = d.createTime ?: "",
                    updateTime = d.updateTime ?: ""
                ))
            } else {
                Result.failure(Exception(response.message ?: "加载失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecord(id: Long, request: RecordUpdateRequest): Result<Unit> {
        return try {
            val response = api.updateRecord(id, request)
            if (response.code == 200) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "修改失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(id: Long): Result<Unit> {
        return try {
            val response = api.deleteRecord(id)
            if (response.code == 200) Result.success(Unit)
            else Result.failure(Exception(response.message ?: "删除失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDiseases(): Result<List<DiseaseInfo>> {
        return try {
            val response = api.getDiseases()
            if (response.code == 200 && response.data != null) {
                val diseases = response.data.map { d ->
                    DiseaseInfo(
                        id = d.id,
                        name = d.name,
                        symptom = d.symptom ?: "",
                        spotFeature = d.spotFeature ?: ""
                    )
                }
                Result.success(diseases)
            } else {
                Result.failure(Exception(response.message ?: "加载失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open image")
        val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return file
    }
}
