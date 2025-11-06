package com.runningcity.data

import com.google.gson.annotations.SerializedName

// 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
/**
 * 워치로부터 받는 운동 데이터 배치
 */
data class WorkoutDataBatch(
    @SerializedName("clientSecretKey")
    val clientSecretKey: String,
    
    @SerializedName("sessionId")
    val sessionId: Long?,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("startTime")
    val startTime: Long,
    
    @SerializedName("endTime")
    val endTime: Long,
    
    @SerializedName("summary")
    val summary: WorkoutSummary,
    
    @SerializedName("cadenceRecords")
    val cadenceRecords: List<CadenceRecord>,
    
    @SerializedName("heartRateRecords")
    val heartRateRecords: List<HeartRateRecord>,
    
    @SerializedName("gpsPoints")
    val gpsPoints: List<GpsPoint>
)

/**
 * 운동 요약 정보
 */
data class WorkoutSummary(
    @SerializedName("totalSteps")
    val totalSteps: Int,
    
    @SerializedName("totalDistance")
    val totalDistance: Double,
    
    @SerializedName("totalCalories")
    val totalCalories: Int,
    
    @SerializedName("avgHeartRate")
    val avgHeartRate: Int,
    
    @SerializedName("duration")
    val duration: Int,
    
    @SerializedName("avgCadence")
    val avgCadence: Int,
    
    @SerializedName("avgPace")
    val avgPace: Int,
    
    @SerializedName("elevation")
    val elevation: Double
)

/**
 * 케이던스 기록
 */
data class CadenceRecord(
    @SerializedName("seq")
    val seq: Long,
    
    @SerializedName("cadence")
    val cadence: Double,
    
    @SerializedName("createdAt")
    val createdAt: Long
)

/**
 * 심박수 기록
 */
data class HeartRateRecord(
    @SerializedName("seq")
    val seq: Long,
    
    @SerializedName("heartRate")
    val heartRate: Int,
    
    @SerializedName("createdAt")
    val createdAt: Long
)

/**
 * GPS 위치 기록
 */
data class GpsPoint(
    @SerializedName("seq")
    val seq: Long,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("altitude")
    val altitude: Double?,
    
    @SerializedName("speed")
    val speed: Float?,
    
    @SerializedName("createdAt")
    val createdAt: Long
)

