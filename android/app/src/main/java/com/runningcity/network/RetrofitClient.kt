//package com.runningcity.network
//
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.scalars.ScalarsConverterFactory
//import retrofit2.converter.gson.GsonConverterFactory
//import com.google.gson.GsonBuilder
//
//object RetrofitClient {
//
//    // 개발/운영 주소 필요에 맞게 수정
//    private const val BASE_URL = "http://10.0.2.2:8080/"  // 에뮬레이터→로컬 PC
//
//    private val gson = GsonBuilder()
//        .setLenient() // 블로그 예시처럼 느슨한 파서 허용
//        .create()
//
//    private val okHttp: OkHttpClient by lazy {
//        val builder = OkHttpClient.Builder()
//        // (선택) 로그 보고 싶으면
//        val logging = HttpLoggingInterceptor().apply {
//            level = HttpLoggingInterceptor.Level.BODY
//        }
//        builder.addInterceptor(logging)
//        builder.build()
//    }
//
//    val instance: Retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            // ⬇️ 순서 중요: 문자열 응답 먼저, JSON 응답 다음
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .client(okHttp)
//            .build()
//    }
//}
