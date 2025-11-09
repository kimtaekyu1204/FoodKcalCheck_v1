package com.example.android.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // 서버 URL 설정 (환경변수 또는 BuildConfig로 설정 가능)
    // 에뮬레이터: http://10.0.2.2:8080/api/
    // 실제 기기: http://{개발자PC_IP}:8080/api/ (예: http://192.168.0.10:8080/api/)
    // 프로덕션(AWS EC2): http://43.203.224.96:8080/api/
    private val BASE_URL = System.getenv("API_BASE_URL") ?: "http://43.203.224.96:8080/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: CheckFoodApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CheckFoodApi::class.java)
    }
}
