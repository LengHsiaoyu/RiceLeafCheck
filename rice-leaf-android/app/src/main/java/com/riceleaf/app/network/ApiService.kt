package com.riceleaf.app.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @GET("diseases/list")
    suspend fun getDiseaseList(): ApiResponse<List<DiseaseDict>>

    @Multipart
    @POST("diseases/detect")
    suspend fun detectDisease(
        @Part image: MultipartBody.Part,
        @Part("samplePoint") samplePoint: RequestBody,
        @Part("plantNo") plantNo: RequestBody,
        @Part("leafPosition") leafPosition: RequestBody,
        @Part("model") model: RequestBody
    ): ApiResponse<DetectResponse>

    @GET("records/count")
    suspend fun getRecordCount(): ApiResponse<Long>

    @GET("records")
    suspend fun getRecords(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<RecordListResponse>

    @GET("records/{id}")
    suspend fun getRecordDetail(@Path("id") id: Long): ApiResponse<RecordItem>

    @PUT("records/{id}")
    suspend fun updateRecord(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): ApiResponse<RecordItem>

    @DELETE("records/{id}")
    suspend fun deleteRecord(@Path("id") id: Long): ApiResponse<Unit>

    @GET("records/export")
    @Streaming
    suspend fun exportRecords(@Query("format") format: String = "xlsx"): ResponseBody
}
