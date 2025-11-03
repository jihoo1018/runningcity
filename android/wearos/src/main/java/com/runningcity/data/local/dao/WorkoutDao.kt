package com.runningcity.data.local.dao

import androidx.room.*
import com.runningcity.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ============ 운동 세션 ============

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity)

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sessions WHERE sessionId = :sessionId")
    suspend fun getSession(sessionId: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("DELETE FROM workout_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)


    // ============ 심박수 기록 ============

    @Insert
    suspend fun insertHeartRate(record: HeartRateRecordEntity)

    @Insert
    suspend fun insertHeartRates(records: List<HeartRateRecordEntity>)

    @Query("SELECT * FROM heart_rate_records WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getHeartRates(sessionId: String): List<HeartRateRecordEntity>

    @Query("SELECT AVG(heartRate) FROM heart_rate_records WHERE sessionId = :sessionId")
    suspend fun getAvgHeartRate(sessionId: String): Int?

    @Query("SELECT MAX(heartRate) FROM heart_rate_records WHERE sessionId = :sessionId")
    suspend fun getMaxHeartRate(sessionId: String): Int?

    @Query("SELECT MIN(heartRate) FROM heart_rate_records WHERE sessionId = :sessionId AND heartRate > 0")
    suspend fun getMinHeartRate(sessionId: String): Int?


    // ============ 위치 기록 ============

    @Insert
    suspend fun insertLocation(record: LocationRecordEntity)

    @Insert
    suspend fun insertLocations(records: List<LocationRecordEntity>)

    @Query("SELECT * FROM location_records WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getLocations(sessionId: String): List<LocationRecordEntity>

    @Query("SELECT COUNT(*) FROM location_records WHERE sessionId = :sessionId")
    suspend fun getLocationCount(sessionId: String): Int
}
