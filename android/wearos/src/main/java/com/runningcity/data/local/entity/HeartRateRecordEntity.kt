package com.runningcity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "heart_rate_records",
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
data class HeartRateRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val watchSessionId: String,
    val timestamp: Long,
    val heartRate: Int,
    val accuracy: Int = 3,  // 0-3, 3이 가장 정확

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val batchId: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)