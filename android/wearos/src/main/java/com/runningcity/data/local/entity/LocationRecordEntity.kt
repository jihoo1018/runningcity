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
            parentColumns = ["seq"],
            childColumns = ["workoutSessionSeq"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("workoutSessionSeq"),
        Index("syncedToServer"),  // 미전송 데이터 조회 최적화
        Index("createdAt")  // 시간순 정렬 최적화
    ]
)
data class LocationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val seq: Long = 0,  // seq로 통일
    val workoutSessionSeq: Long,  // ✅ WorkoutSession의 seq 참조

    val createdAt: Long,  // ✅ 워치 에서 위치가 측정된 시각
    val latitude: Double,  // 위도 (예: 37.5665 = 서울 시청)
    val longitude: Double,  // 경도 (예: 126.9780 = 서울 시청)
    val accuracy: Float,  // GPS 정확도, 미터 단위 (예: 5.0 = ±5m 오차)
    val altitude: Double? = null,  // 고도, 미터 단위 (예: 125.5 = 해발 125.5m)
    val speed: Float? = null,  // 속도, m/s 단위 (예: 3.33 = 약 12km/h)

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val batchId: String? = null,  // 배치 전송 그룹 ID

    val savedAt: Long = System.currentTimeMillis()  // ✅ DB에 저장된 시각
)