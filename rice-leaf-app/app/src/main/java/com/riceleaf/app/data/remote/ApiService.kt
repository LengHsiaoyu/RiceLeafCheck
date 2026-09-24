package com.riceleaf.app.data.remote

import com.riceleaf.app.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("diseases/detect")
    suspend fun detect(
        @Part image: MultipartBody.Part,
        @Part("samplePoint") samplePoint: okhttp3.RequestBody,
        @Part("plantNo") plantNo: okhttp3.RequestBody,
        @Part("leafPosition") leafPosition: okhttp3.RequestBody
    ): ApiResponse<DetectResponse>

    @GET("records")
    suspend fun getRecords(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("diseaseId") diseaseId: Long? = null
    ): ApiResponse<RecordListResponse>

    @GET("records/{id}")
    suspend fun getRecordDetail(@Path("id") id: Long): ApiResponse<RecordDetailResponse>

    @PUT("records/{id}")
    suspend fun updateRecord(
        @Path("id") id: Long,
        @Body request: RecordUpdateRequest
    ): ApiResponse<Any>

    @DELETE("records/{id}")
    suspend fun deleteRecord(@Path("id") id: Long): ApiResponse<Any>

    @GET("records/export")
    @Streaming
    suspend fun exportRecords(
        @Query("format") format: String = "xlsx",
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ResponseBody

    @GET("diseases/list")
    suspend fun getDiseases(): ApiResponse<List<DiseaseDict>>
}
