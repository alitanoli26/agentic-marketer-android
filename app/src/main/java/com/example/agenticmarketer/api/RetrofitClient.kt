package com.example.agenticmarketer.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val OPENROUTER_BASE_URL = "https://openrouter.ai/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val HUGGINGFACE_BASE_URL = "https://router.huggingface.co/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Free-tier image models on Hugging Face can have a "cold start" where the
    // underlying model needs to spin up on the provider's GPU before it can
    // serve a request — this can take well over a minute the first time, so
    // we give this client a much longer read timeout than the others.
    private val longTimeoutClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val openRouterApi: OpenRouterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPENROUTER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(OpenRouterApiService::class.java)
    }

    val geminiApi: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GeminiApiService::class.java)
    }

    val huggingFaceApi: HuggingFaceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(HUGGINGFACE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(longTimeoutClient)
            .build()
            .create(HuggingFaceApiService::class.java)
    }
}
