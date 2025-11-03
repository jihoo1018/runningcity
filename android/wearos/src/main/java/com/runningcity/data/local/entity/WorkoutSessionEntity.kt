package com.runningcity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey
    val sessionId: String,

    // 기본 정보
    val userId: String = "default_user",
    val exerciseType: String = "RUNNING",
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

    // 메타데이터
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)