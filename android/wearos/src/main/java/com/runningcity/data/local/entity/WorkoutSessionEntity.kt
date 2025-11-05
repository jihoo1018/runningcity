package com.runningcity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "workout_sessions",
    indices = [
        Index(value = ["watchSessionId"], unique = true),
        Index(value = ["syncedToServer", "status"]) // 동기화 쿼리 최적화
    ]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 세션 식별자
    val watchSessionId: String,  // 워치에서 생성한 고유 ID (UUID)
    val sessionId: String? = null,  // 서버에서 받은 ID

    // 기본 정보
    val userId: String = "default_user",
//    val exerciseType: String = "RUNNING",
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Int? = null,  // 초
    val status: String = "IN_PROGRESS",  // IN_PROGRESS, COMPLETED, PAUSED

    // 요약 데이터
    val totalSteps: Int = 0,
    val totalDistance: Double = 0.0,  // 미터
    val totalCalories: Int = 0,
    val avgHeartRate: Int = 0,
    val maxHeartRate: Int = 0,
    val minHeartRate: Int = 999,
    val avgCadence: Int = 0,

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val syncRetryCount: Int = 0,

    // 메타데이터
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)