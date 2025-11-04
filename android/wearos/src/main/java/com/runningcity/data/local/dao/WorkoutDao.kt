package com.runningcity.data.local.dao

import androidx.room.*
import com.runningcity.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ==================== 운동 세션 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(session: WorkoutSessionEntity)

    // watchSessionId로 조회 (주요 사용)
    @Query("SELECT * FROM workout_sessions WHERE watchSessionId = :watchSessionId")
    suspend fun getSession(watchSessionId: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE watchSessionId = :watchSessionId")
    fun getSessionFlow(watchSessionId: String): Flow<WorkoutSessionEntity?>

    // 전체 세션 목록 (최신순)
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int = 20): List<WorkoutSessionEntity>

    // 동기화 관련
    @Query("SELECT * FROM workout_sessions WHERE syncedToServer = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedSessions(): List<WorkoutSessionEntity>

    @Query("UPDATE workout_sessions SET syncedToServer = 1, sessionId = :sessionId WHERE watchSessionId = :watchSessionId")
    suspend fun markSessionAsSynced(watchSessionId: String, sessionId: String)

    @Query("UPDATE workout_sessions SET lastSyncAttempt = :timestamp, syncRetryCount = syncRetryCount + 1 WHERE watchSessionId = :watchSessionId")
    suspend fun updateSyncAttempt(watchSessionId: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE syncedToServer = 0")
    suspend fun getUnsyncedSessionCount(): Int

    @Query("DELETE FROM workout_sessions WHERE watchSessionId = :watchSessionId")
    suspend fun deleteSession(watchSessionId: String)


    // ==================== 심박수 기록 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRate(record: HeartRateRecordEntity): Long

    @Insert
    suspend fun insertHeartRates(records: List<HeartRateRecordEntity>): List<Long>

    // watchSessionId로 조회
    @Query("SELECT * FROM heart_rate_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    suspend fun getHeartRates(watchSessionId: String): List<HeartRateRecordEntity>

    @Query("SELECT * FROM heart_rate_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    fun getHeartRatesFlow(watchSessionId: String): Flow<List<HeartRateRecordEntity>>

    // 통계 쿼리
    @Query("SELECT AVG(heartRate) FROM heart_rate_records WHERE watchSessionId = :watchSessionId")
    suspend fun getAvgHeartRate(watchSessionId: String): Int?

    @Query("SELECT MAX(heartRate) FROM heart_rate_records WHERE watchSessionId = :watchSessionId")
    suspend fun getMaxHeartRate(watchSessionId: String): Int?

    @Query("SELECT MIN(heartRate) FROM heart_rate_records WHERE watchSessionId = :watchSessionId AND heartRate > 0")
    suspend fun getMinHeartRate(watchSessionId: String): Int?

    @Query("""
        SELECT 
            AVG(heartRate) as avgHeartRate,
            MAX(heartRate) as maxHeartRate,
            MIN(heartRate) as minHeartRate
        FROM heart_rate_records 
        WHERE watchSessionId = :watchSessionId
    """)
    suspend fun getHeartRateStats(watchSessionId: String): HeartRateStats?

    // 배치 전송용 쿼리
    @Query("SELECT * FROM heart_rate_records WHERE syncedToServer = 0 ORDER BY id ASC LIMIT :limit")
    suspend fun getUnsyncedHeartRates(limit: Int = 100): List<HeartRateRecordEntity>

    @Query("UPDATE heart_rate_records SET syncedToServer = 1, batchId = :batchId WHERE id IN (:ids)")
    suspend fun markHeartRatesAsSynced(ids: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM heart_rate_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedHeartRateCount(): Int

    // 메모리 관리 (오래된 동기화된 데이터 삭제)
    @Query("DELETE FROM heart_rate_records WHERE syncedToServer = 1 AND createdAt < :beforeTimestamp")
    suspend fun deleteOldSyncedHeartRates(beforeTimestamp: Long)


    // ==================== 위치 기록 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(record: LocationRecordEntity): Long

    @Insert
    suspend fun insertLocations(records: List<LocationRecordEntity>): List<Long>

    // watchSessionId로 조회
    @Query("SELECT * FROM location_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    suspend fun getLocations(watchSessionId: String): List<LocationRecordEntity>

    @Query("SELECT * FROM location_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    fun getLocationsFlow(watchSessionId: String): Flow<List<LocationRecordEntity>>

    @Query("SELECT COUNT(*) FROM location_records WHERE watchSessionId = :watchSessionId")
    suspend fun getLocationCount(watchSessionId: String): Int

    // 배치 전송용 쿼리 (ID 기반 범위 조회)
    @Query("SELECT * FROM location_records WHERE syncedToServer = 0 AND id BETWEEN :startId AND :endId ORDER BY id ASC")
    suspend fun getUnsyncedLocationsByIdRange(startId: Long, endId: Long): List<LocationRecordEntity>

    @Query("SELECT * FROM location_records WHERE syncedToServer = 0 ORDER BY id ASC LIMIT :limit")
    suspend fun getUnsyncedLocations(limit: Int = 100): List<LocationRecordEntity>

    @Query("UPDATE location_records SET syncedToServer = 1, batchId = :batchId WHERE id IN (:ids)")
    suspend fun markLocationsAsSynced(ids: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM location_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedLocationCount(): Int

    @Query("DELETE FROM location_records WHERE watchSessionId = :watchSessionId")
    suspend fun deleteLocationRecords(watchSessionId: String)

    @Query("DELETE FROM location_records WHERE syncedToServer = 1 AND createdAt < :beforeTimestamp")
    suspend fun deleteOldSyncedLocations(beforeTimestamp: Long)


    // ==================== 케이던스 기록 (신규) ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCadence(record: CadenceRecordEntity): Long

    @Insert
    suspend fun insertCadences(records: List<CadenceRecordEntity>): List<Long>

    @Query("SELECT * FROM cadence_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    suspend fun getCadences(watchSessionId: String): List<CadenceRecordEntity>

    @Query("SELECT * FROM cadence_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    fun getCadencesFlow(watchSessionId: String): Flow<List<CadenceRecordEntity>>

    @Query("SELECT AVG(cadence) FROM cadence_records WHERE watchSessionId = :watchSessionId")
    suspend fun getAvgCadence(watchSessionId: String): Int?

    // 배치 전송용
    @Query("SELECT * FROM cadence_records WHERE syncedToServer = 0 ORDER BY id ASC LIMIT :limit")
    suspend fun getUnsyncedCadences(limit: Int = 100): List<CadenceRecordEntity>

    @Query("UPDATE cadence_records SET syncedToServer = 1, batchId = :batchId WHERE id IN (:ids)")
    suspend fun markCadencesAsSynced(ids: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM cadence_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedCadenceCount(): Int

    @Query("DELETE FROM cadence_records WHERE syncedToServer = 1 AND createdAt < :beforeTimestamp")
    suspend fun deleteOldSyncedCadences(beforeTimestamp: Long)


    // ==================== 칼로리 기록 (신규) ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalorie(record: CalorieRecordEntity): Long

    @Insert
    suspend fun insertCalories(records: List<CalorieRecordEntity>): List<Long>

    @Query("SELECT * FROM calorie_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    suspend fun getCalories(watchSessionId: String): List<CalorieRecordEntity>

    @Query("SELECT * FROM calorie_records WHERE watchSessionId = :watchSessionId ORDER BY timestamp ASC")
    fun getCaloriesFlow(watchSessionId: String): Flow<List<CalorieRecordEntity>>

    @Query("SELECT MAX(calories) FROM calorie_records WHERE watchSessionId = :watchSessionId")
    suspend fun getTotalCalories(watchSessionId: String): Double?

    // 배치 전송용
    @Query("SELECT * FROM calorie_records WHERE syncedToServer = 0 ORDER BY id ASC LIMIT :limit")
    suspend fun getUnsyncedCalories(limit: Int = 100): List<CalorieRecordEntity>

    @Query("UPDATE calorie_records SET syncedToServer = 1, batchId = :batchId WHERE id IN (:ids)")
    suspend fun markCaloriesAsSynced(ids: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM calorie_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedCalorieCount(): Int

    @Query("DELETE FROM calorie_records WHERE syncedToServer = 1 AND createdAt < :beforeTimestamp")
    suspend fun deleteOldSyncedCalories(beforeTimestamp: Long)


    // ==================== 통합 쿼리 ====================

    // 특정 세션의 모든 데이터 삭제
    @Transaction
    suspend fun deleteSessionWithAllData(watchSessionId: String) {
        deleteLocationRecords(watchSessionId)
        deleteSession(watchSessionId)
        // CASCADE로 나머지는 자동 삭제됨
    }

    // 모든 미동기화 데이터 개수 조회
    suspend fun getAllUnsyncedCount(): Int {
        return getUnsyncedSessionCount() +
                getUnsyncedLocationCount() +
                getUnsyncedHeartRateCount() +
                getUnsyncedCadenceCount() +
                getUnsyncedCalorieCount()
    }
}

// ==================== 데이터 클래스 ====================

data class HeartRateStats(
    val avgHeartRate: Int,
    val maxHeartRate: Int,
    val minHeartRate: Int
)