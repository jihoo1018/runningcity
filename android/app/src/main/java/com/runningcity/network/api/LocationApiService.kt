package com.runningcity.network.api

import com.runningcity.network.model.LocationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * LocationApiService
 * - 서버의 위치 전송 API 인터페이스
 * - Retrofit이 내부적으로 HTTP 요청을 자동 처리
 */
interface LocationApiService {

    @POST("/api/v1/location")
    suspend fun sendLocation(
        @Body request: LocationRequest
    ): Response<Unit> // 본문이 필요 없는 경우 Response<Unit> 사용
}
