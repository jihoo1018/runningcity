package com.runningcity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "workout_sessions",
    indices = [
        Index(value = ["clientSecretKey"], unique = true),  // ✅ 이름 유지
        Index(value = ["syncedToServer", "status"])
    ]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val seq: Long = 0,

    // 세션 식별자
    val clientSecretKey: String,  // 워치에서 생성한 고유 ID (UUID)
    //추가
    val sessionId : Long = 0, //세션에서 받아오는 sessionid(모바일 시작 시에만 생성됨),

    // 기본 정보
    val userId: String = "default_user",
    val startTime: Long,  // 운동 시작 시각 (밀리초)
    val endTime: Long? = null,  // 운동 종료 시각 (밀리초)
    val status: String = "IN_PROGRESS",  // IN_PROGRESS, COMPLETED, PAUSED

    // 요약 데이터
    val totalSteps: Int = 0,  // 총 걸음 수 (예: 5280 = 5,280걸음)
    val totalDistance: Double = 0.0,  // 총 거리, 미터 단위 (예: 5000.0 = 5km)
    val totalCalories: Int = 0,  // 소모 칼로리, kcal 단위 (예: 450 = 450kcal)
    val avgHeartRate: Int = 0,  // 평균 심박수, bpm 단위 (예: 145 = 145bpm)
    val duration: Int? = null,  // 운동 지속 시간, 초 단위 (예: 1800 = 30분)
    val avgCadence: Int = 0,  // 평균 케이던스, spm 단위 (예: 170 = 170 steps/min)
    val avgPace: Int = 0,  // 평균 페이스, 초/km 단위 (예: 300 = 5분/km, 330 = 5분 30초/km)
    val elevation: Double = 0.0,  // 평균 고도, 미터 단위 (예: 125.5 = 해발 125.5m)

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val syncRetryCount: Int = 0,

    // 메타데이터
    val createdAt: Long = System.currentTimeMillis(),  // 세션 생성 시각 (워치 기준)
    val updatedAt: Long = System.currentTimeMillis()  // 마지막 수정 시각
)