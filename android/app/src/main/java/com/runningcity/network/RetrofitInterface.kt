package com.runningcity.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

// 서버에 어떤 API가 있는지 선언만 하는 곳
interface YOUR_API_INTERFACE {

    // ───────────── GET ─────────────
    @GET("ENDPOINT_PATH") // 예: "users", "api/v1/running"
    fun getSomething(
        @Query("QUERY_KEY_1") queryParam1: QUERY_TYPE_1,
        @Query("QUERY_KEY_2") queryParam2: QUERY_TYPE_2,
    ): Call<RESPONSE_DTO>

    // ───────────── GET with path ─────────────
    @GET("ENDPOINT_PATH/{PATH_VAR}")
    fun getSomethingById(
        @Path("PATH_VAR") id: PATH_TYPE,
    ): Call<RESPONSE_DTO>

    // ───────────── POST(JSON Body) ─────────────
    @POST("ENDPOINT_PATH")
    fun postSomething(
        @Body body: REQUEST_DTO
    ): Call<RESPONSE_DTO>

    // ───────────── Form POST ─────────────
    @FormUrlEncoded
    @POST("ENDPOINT_PATH")
    fun postForm(
        @Field("FIELD_KEY_1") field1: FIELD_TYPE_1,
        @Field("FIELD_KEY_2") field2: FIELD_TYPE_2,
    ): Call<RESPONSE_DTO>
}

//YOUR_PACKAGE_HERE → 패키지 경로
// YOUR_RESPONSE_NAME → 응답용 DTO 이름
// YOUR_REQUEST_NAME → 요청 보낼 때 DTO 이름
// JSON_KEY_1 → 서버가 실제로 주는 JSON의 키 이름
// FIELD_NAME_1 → 코틀린에서 쓰고 싶은 변수 이름
// FIELD_TYPE_1 → String, Int, Long, Double, Boolean, List<...> 이런 실제 타입
// ENDPOINT_PATH → "users", "api/v1/records" 같은 URL 뒷부분
// RESPONSE_DTO → 위에서 만든 응답 모델 이름
// REQUEST_DTO → 위에서 만든 요청 모델 이름