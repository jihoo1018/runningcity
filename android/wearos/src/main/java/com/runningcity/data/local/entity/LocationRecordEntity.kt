package com.runningcity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "location_records",
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
        Index("syncedToServer"),  // 미전송 데이터 조회 최적화
        Index("timestamp")  // 시간순 정렬 최적화
    ]
)
data class LocationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,  // BigInt로 자동 증가 (배치 전송시 범위 지정용)

    val watchSessionId: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val speed: Float? = null,

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val batchId: String? = null,  // 배치 전송 그룹 ID

    val createdAt: Long = System.currentTimeMillis()
)