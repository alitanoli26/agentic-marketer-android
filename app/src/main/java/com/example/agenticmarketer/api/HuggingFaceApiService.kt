package com.example.agenticmarketer.api

import com.example.agenticmarketer.models.HuggingFaceImageRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface HuggingFaceApiService {
    @POST("hf-inference/models/{model}")
    suspend fun generateImage(
        @Path("model") model: String,
        @Header("Authorization") auth: String,
        @Body request: HuggingFaceImageRequest
    ): Response<ResponseBody>
}