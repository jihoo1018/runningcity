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

    // seq로 조회 (PK 조회)
    @Query("SELECT * FROM workout_sessions WHERE seq = :seq")
    suspend fun getSessionBySeq(seq: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE seq = :seq")
    fun getSessionFlowBySeq(seq: Long): Flow<WorkoutSessionEntity?>

    // clientSecretKey로 조회 (UUID 조회)
    @Query("SELECT * FROM workout_sessions WHERE clientSecretKey = :clientSecretKey")
    suspend fun getSession(clientSecretKey: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE clientSecretKey = :clientSecretKey")
    fun getSessionFlow(clientSecretKey: String): Flow<WorkoutSessionEntity?>

    // 전체 세션 목록 (최신순)
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int = 20): List<WorkoutSessionEntity>

    // 동기화 관련
    @Query("SELECT * FROM workout_sessions WHERE syncedToServer = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedSessions(): List<WorkoutSessionEntity>

    @Query("UPDATE workout_sessions SET syncedToServer = 1 WHERE clientSecretKey = :clientSecretKey")
    suspend fun markSessionAsSynced(clientSecretKey: String)

    @Query("UPDATE workout_sessions SET lastSyncAttempt = :timestamp, syncRetryCount = syncRetryCount + 1 WHERE clientSecretKey = :clientSecretKey")
    suspend fun updateSyncAttempt(clientSecretKey: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE syncedToServer = 0")
    suspend fun getUnsyncedSessionCount(): Int

    @Query("DELETE FROM workout_sessions WHERE clientSecretKey = :clientSecretKey")
    suspend fun deleteSession(clientSecretKey: String)


    // ==================== 심박수 기록 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeartRate(record: HeartRateRecordEntity): Long

    @Insert
    suspend fun insertHeartRates(records: List<HeartRateRecordEntity>): List<Long>

    // workoutSessionSeq로 조회 (FK 기반)
    @Query("SELECT * FROM heart_rate_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    suspend fun getHeartRates(workoutSessionSeq: Long): List<HeartRateRecordEntity>

    @Query("SELECT * FROM heart_rate_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    fun getHeartRatesFlow(workoutSessionSeq: Long): Flow<List<HeartRateRecordEntity>>

    // 통계 쿼리
    @Query("SELECT AVG(heartRate) FROM heart_rate_records WHERE workoutSessionSeq = :workoutSessionSeq")
    suspend fun getAvgHeartRate(workoutSessionSeq: Long): Int?

    @Query("SELECT MAX(heartRate) FROM heart_rate_records WHERE workoutSessionSeq = :workoutSessionSeq")
    suspend fun getMaxHeartRate(workoutSessionSeq: Long): Int?

    @Query("""
        SELECT 
            AVG(heartRate) as avgHeartRate,
            MAX(heartRate) as maxHeartRate,
            MIN(CASE WHEN heartRate > 0 THEN heartRate END) as minHeartRate
        FROM heart_rate_records 
        WHERE workoutSessionSeq = :workoutSessionSeq
    """)
    suspend fun getHeartRateStats(workoutSessionSeq: Long): HeartRateStats?

    // 배치 전송용 쿼리
    @Query("SELECT * FROM heart_rate_records WHERE syncedToServer = 0 ORDER BY seq ASC LIMIT :limit")
    suspend fun getUnsyncedHeartRates(limit: Int = 100): List<HeartRateRecordEntity>

    @Query("UPDATE heart_rate_records SET syncedToServer = 1, batchId = :batchId WHERE seq IN (:seqs)")
    suspend fun markHeartRatesAsSynced(seqs: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM heart_rate_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedHeartRateCount(): Int


    // ==================== 위치 기록 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(record: LocationRecordEntity): Long

    @Insert
    suspend fun insertLocations(records: List<LocationRecordEntity>): List<Long>

    // workoutSessionSeq로 조회
    @Query("SELECT * FROM location_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    suspend fun getLocations(workoutSessionSeq: Long): List<LocationRecordEntity>

    @Query("SELECT * FROM location_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    fun getLocationsFlow(workoutSessionSeq: Long): Flow<List<LocationRecordEntity>>

    @Query("SELECT COUNT(*) FROM location_records WHERE workoutSessionSeq = :workoutSessionSeq")
    suspend fun getLocationCount(workoutSessionSeq: Long): Int

    // 배치 전송용 쿼리
    @Query("SELECT * FROM location_records WHERE syncedToServer = 0 ORDER BY seq ASC LIMIT :limit")
    suspend fun getUnsyncedLocations(limit: Int = 100): List<LocationRecordEntity>

    @Query("UPDATE location_records SET syncedToServer = 1, batchId = :batchId WHERE seq IN (:seqs)")
    suspend fun markLocationsAsSynced(seqs: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM location_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedLocationCount(): Int

    @Query("DELETE FROM location_records WHERE workoutSessionSeq = :workoutSessionSeq")
    suspend fun deleteLocationRecords(workoutSessionSeq: Long)


    // ==================== 케이던스 기록 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCadence(record: CadenceRecordEntity): Long

    @Insert
    suspend fun insertCadences(records: List<CadenceRecordEntity>): List<Long>

    @Query("SELECT * FROM cadence_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    suspend fun getCadences(workoutSessionSeq: Long): List<CadenceRecordEntity>

    @Query("SELECT * FROM cadence_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    fun getCadencesFlow(workoutSessionSeq: Long): Flow<List<CadenceRecordEntity>>

    @Query("SELECT AVG(cadence) FROM cadence_records WHERE workoutSessionSeq = :workoutSessionSeq")
    suspend fun getAvgCadence(workoutSessionSeq: Long): Int?

    // 배치 전송용
    @Query("SELECT * FROM cadence_records WHERE syncedToServer = 0 ORDER BY seq ASC LIMIT :limit")
    suspend fun getUnsyncedCadences(limit: Int = 100): List<CadenceRecordEntity>

    @Query("UPDATE cadence_records SET syncedToServer = 1, batchId = :batchId WHERE seq IN (:seqs)")
    suspend fun markCadencesAsSynced(seqs: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM cadence_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedCadenceCount(): Int


    // ==================== 칼로리 기록 ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalorie(record: CalorieRecordEntity): Long

    @Insert
    suspend fun insertCalories(records: List<CalorieRecordEntity>): List<Long>

    @Query("SELECT * FROM calorie_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    suspend fun getCalories(workoutSessionSeq: Long): List<CalorieRecordEntity>

    @Query("SELECT * FROM calorie_records WHERE workoutSessionSeq = :workoutSessionSeq ORDER BY createdAt ASC")
    fun getCaloriesFlow(workoutSessionSeq: Long): Flow<List<CalorieRecordEntity>>

    @Query("SELECT MAX(calories) FROM calorie_records WHERE workoutSessionSeq = :workoutSessionSeq")
    suspend fun getTotalCalories(workoutSessionSeq: Long): Double?

    // 배치 전송용
    @Query("SELECT * FROM calorie_records WHERE syncedToServer = 0 ORDER BY seq ASC LIMIT :limit")
    suspend fun getUnsyncedCalories(limit: Int = 100): List<CalorieRecordEntity>

    @Query("UPDATE calorie_records SET syncedToServer = 1, batchId = :batchId WHERE seq IN (:seqs)")
    suspend fun markCaloriesAsSynced(seqs: List<Long>, batchId: String)

    @Query("SELECT COUNT(*) FROM calorie_records WHERE syncedToServer = 0")
    suspend fun getUnsyncedCalorieCount(): Int


    // ==================== 통합 쿼리 ====================

    // 특정 세션의 모든 데이터 삭제 (seq 기반)
    @Transaction
    suspend fun deleteSessionWithAllData(workoutSessionSeq: Long) {
        deleteLocationRecords(workoutSessionSeq)
        // CASCADE로 나머지는 자동 삭제됨
        val session = getSessionBySeq(workoutSessionSeq)
        session?.let {
            deleteSession(it.clientSecretKey)  // ✅ 소문자로 통일
        }
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
    val minHeartRate: Int?
)