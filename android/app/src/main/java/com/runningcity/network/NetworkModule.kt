package com.runningcity.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * NetworkModule
 * - Retrofit 및 OkHttp 설정을 관리하는 싱글턴 객체
 * - 모든 네트워크 요청이 이 인스턴스를 공유함
 */
object NetworkModule {

    // TODO: ngrok URL은 매번 새로 켤 때 갱신 필요
    private const val BASE_URL = "https://castiel-propraetorial-hadlee.ngrok-free.dev"

    // OkHttpClient: 타임아웃 및 기본 설정
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // Retrofit 인스턴스 생성
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create()) // JSON 직렬화
        .client(okHttpClient)
        .build()
}
