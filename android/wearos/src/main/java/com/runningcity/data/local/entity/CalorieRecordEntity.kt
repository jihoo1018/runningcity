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
            parentColumns = ["watchSessionId"],
            childColumns = ["watchSessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("watchSessionId"),
        Index("syncedToServer"),
        Index("timestamp")
    ]
)
data class CalorieRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val watchSessionId: String,
    val timestamp: Long,
    val calories: Double,  // 누적 칼로리 (kcal)
    val caloriesIncrement: Double? = null,  // 이전 기록 대비 증가량

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val batchId: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)