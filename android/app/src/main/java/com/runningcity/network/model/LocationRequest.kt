package com.runningcity.network.model

/**
 * LocationRequest
 * - 서버에 보낼 위치 데이터 모델
 * - Retrofit이 자동으로 JSON으로 변환해 전송함
 */
data class LocationRequest(
    val latitude: Double,     // 위도
    val longitude: Double,    // 경도
    val accuracy: Float,      // 측정 정확도 (m)
    val altitude: Double,     // 고도 (m)
    val speed: Float,         // 속도 (m/s)
    val bearing: Float,       // 진행 방향 (도 단위)
    val provider: String?,    // 위치 제공자 (gps / network)
    val timestamp: Long       // 측정 시각 (millisecond)
)
