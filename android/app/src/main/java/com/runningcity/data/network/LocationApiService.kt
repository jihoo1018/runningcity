package com.runningcity.data.network

import com.runningcity.data.model.LocationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 🌐 LocationApiService
 * ────────────────────────────────────────────────
 * - 서버와 통신하는 Retrofit API 인터페이스
 * - 세션 종료 시 모든 위치 데이터를 한번에 업로드
 * ────────────────────────────────────────────────
 */
interface LocationApiService {
    @POST("/api/v1/location/batch")
    suspend fun sendLocationBatch(@Body batch: List<LocationDto>): Response<Unit>
}
