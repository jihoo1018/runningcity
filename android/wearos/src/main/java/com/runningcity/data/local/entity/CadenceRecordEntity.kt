package com.runningcity.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cadence_records",
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
        Index("createdAt")
    ]
)
data class CadenceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val seq: Long = 0,

    val workoutSessionSeq: Long,  // ✅ WorkoutSession의 seq 참조
    val createdAt: Long,  // ✅ 워치에서 케이던스가 측정된 시각
    val cadence: Int,  // SPM (Steps Per Minute)

    // 동기화 관리
    val syncedToServer: Boolean = false,
    val lastSyncAttempt: Long? = null,
    val batchId: String? = null,

    val savedAt: Long = System.currentTimeMillis()  // ✅ DB에 저장된 시각
)