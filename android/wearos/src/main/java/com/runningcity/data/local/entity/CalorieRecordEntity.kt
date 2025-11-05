package com.runningcity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "calorie_records",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["seq"],
            childColumns = ["workoutSessionSeq"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workoutSessionSeq"),
        Index("syncedToServer"),
        Index("createdAt")  // ✅ 인덱스명도 변경
    ]
)
data class CalorieRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val seq: Long = 0,

    val workoutSessionSeq: Long,  // ✅ WorkoutSession의 seq 참조
    val createdAt: Long,  // ✅ 실제 운동 중 칼로리가 측정된 시각 (워치 기준)
    val calories: Double,  // 누적 칼로리 (kcal)
    val caloriesIncrement: Double? = null,  // 이전 기록 대비 증가량

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val batchId: String? = null,

    val savedAt: Long = System.currentTimeMillis()  // ✅ DB에 저장된 시각
)