package com.runningcity.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import com.runningcity.data.local.WorkoutDatabase
import com.runningcity.data.local.entity.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

// 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
/**
 * WatchDataSyncRepository
 * 
 * 워치의 Room DB 데이터를 모바일로 전송하는 레포지토리
 * 
 * 주요 기능:
 * 1. Room DB에서 미동기화 데이터 조회 (syncedToServer = false)
 * 2. 300개씩 배치로 묶어서 JSON 직렬화
 * 3. Data Layer API로 모바일에 전송
 * 4. 전송 성공 시 syncedToServer = true로 업데이트
 * 
 * 특징:
 * - 모바일이 꺼져있어도 Data Layer가 자동으로 큐에 쌓음
 * - 모바일이 켜지면 자동으로 전송됨
 * - 워치는 계속 Room DB에 데이터를 쌓으면 됨
 * 
 * 순서 보장:
 * - id 자동 증가로 삽입 순서 보장
 * - ORDER BY id ASC로 가장 오래된 것부터 전송
 * - Mutex로 동시 실행 방지 (순서 보장 강화)
 */
class WatchDataSyncRepository(
    private val context: Context,
    private val database: WorkoutDatabase
) {
    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val nodeClient: NodeClient = Wearable.getNodeClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)
    
    private val gson = Gson()
    
    // 동시 실행 방지를 위한 Mutex (순서 보장 강화)
    private val syncMutex = Mutex()
    
    companion object {
        private const val TAG = "WatchDataSync"
        private const val DEFAULT_BATCH_SIZE = 300
        private const val PATH_WORKOUT_DATA = "/workout_data"
        private const val CAPABILITY_MOBILE = "running_city_mobile"
    }

    /**
     * 미동기화 데이터를 모바일로 전송
     * 
     * 순서 보장 메커니즘:
     * 1. Mutex로 동시 실행 방지 (한 번에 하나씩만 전송)
     * 2. ORDER BY id ASC로 가장 오래된 것부터 조회
     * 3. 전송 성공 후에만 syncedToServer = true 업데이트
     * 4. 실패 시 그대로 유지 → 다음 전송 시 재시도
     * 
     * @param watchSessionId 세션 ID (null이면 모든 세션)
     * @param batchSize 배치 크기 (기본 300개, 실시간 모드에서는 1개)
     * @return 전송 성공 여부
     */
    suspend fun syncWorkoutData(
        watchSessionId: String? = null,
        batchSize: Int = DEFAULT_BATCH_SIZE
    ): SyncResult {
        // 🔒 동시 실행 방지 - 순서 보장의 핵심!
        return syncMutex.withLock {
            syncWorkoutDataInternal(watchSessionId, batchSize)
        }
    }
    
    /**
     * 실제 동기화 로직 (Mutex 내부에서 실행)
     */
    private suspend fun syncWorkoutDataInternal(
        watchSessionId: String?,
        batchSize: Int
    ): SyncResult {
        return try {
            // 1. 모바일 연결 확인 (선택사항)
            val isMobileReachable = checkMobileConnection()
            Log.d(TAG, "모바일 연결 상태: $isMobileReachable")
            
            // 2. 미동기화 데이터 조회
            val heartRates = if (watchSessionId != null) {
                database.workoutDao().getUnsyncedHeartRates(batchSize)
                    .filter { it.watchSessionId == watchSessionId }
            } else {
                database.workoutDao().getUnsyncedHeartRates(batchSize)
            }
            
            val locations = if (watchSessionId != null) {
                database.workoutDao().getUnsyncedLocations(batchSize)
                    .filter { it.watchSessionId == watchSessionId }
            } else {
                database.workoutDao().getUnsyncedLocations(batchSize)
            }
            
            val cadences = if (watchSessionId != null) {
                database.workoutDao().getUnsyncedCadences(batchSize)
                    .filter { it.watchSessionId == watchSessionId }
            } else {
                database.workoutDao().getUnsyncedCadences(batchSize)
            }
            
            val calories = if (watchSessionId != null) {
                database.workoutDao().getUnsyncedCalories(batchSize)
                    .filter { it.watchSessionId == watchSessionId }
            } else {
                database.workoutDao().getUnsyncedCalories(batchSize)
            }
            
            // 3. 전송할 데이터가 없으면 종료
            if (heartRates.isEmpty() && locations.isEmpty() && 
                cadences.isEmpty() && calories.isEmpty()) {
                Log.d(TAG, "전송할 데이터 없음")
                return SyncResult.NoData
            }
            
            // 4. 세션 정보 조회
            val session = watchSessionId?.let { 
                database.workoutDao().getSession(it)
            } ?: return SyncResult.Error("세션 정보를 찾을 수 없습니다")
            
            // 5. 거리 기반 평균 페이스 계산 (초/km)
            val avgPace = if (session.totalDistance > 0 && session.duration != null) {
                ((session.duration.toDouble() / (session.totalDistance / 1000.0))).toInt()
            } else {
                0
            }
            
            // 6. 평균 고도 계산
            val avgElevation = if (locations.isNotEmpty()) {
                locations.mapNotNull { it.altitude }.average()
            } else {
                0.0
            }
            
            // 7. WorkoutDataBatch 생성
            val batch = WorkoutDataBatch(
                clientSecretKey = session.watchSessionId,
                sessionId = null,  // 워치에서 시작한 경우 null
                userId = session.userId,
                startTime = session.startTime,
                endTime = session.endTime ?: System.currentTimeMillis(),
                summary = WorkoutSummary(
                    totalSteps = session.totalSteps,
                    totalDistance = session.totalDistance,
                    totalCalories = session.totalCalories,
                    avgHeartRate = session.avgHeartRate,
                    duration = session.duration ?: 0,
                    avgCadence = session.avgCadence,
                    avgPace = avgPace,
                    elevation = avgElevation
                ),
                cadenceRecords = cadences.map { convertToCadence(it) },
                heartRateRecords = heartRates.map { convertToHeartRate(it) },
                gpsPoints = locations.map { convertToGpsPoint(it) }
            )
            
            // 8. JSON 직렬화
            val jsonString = gson.toJson(batch)
            
            // 9. Data Layer API로 전송
            val success = sendToMobile(session.watchSessionId, jsonString)
            
            if (success) {
                // 10. 전송 성공 시에만 DB 업데이트
                try {
                    markAsSynced(heartRates, locations, cadences, calories, session.watchSessionId)
                    
                    SyncResult.Success(
                        clientSecretKey = session.watchSessionId,
                        heartRateCount = heartRates.size,
                        gpsCount = locations.size,
                        cadenceCount = cadences.size
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ DB 업데이트 실패: ${e.message}")
                    SyncResult.Error("DB update failed: ${e.message}")
                }
            } else {
                Log.w(TAG, "⚠️ 전송 실패 - 다음 전송 시 재시도")
                SyncResult.Queued(session.watchSessionId)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 동기화 실패: ${e.message}", e)
            SyncResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * 모바일 기기 연결 확인 (Public)
     * @return true: 모바일이 연결됨, false: 연결 해제
     */
    suspend fun isMobileReachable(): Boolean {
        return checkMobileConnection()
    }
    
    /**
     * 모바일 연결 상태 확인 (Internal)
     */
    private suspend fun checkMobileConnection(): Boolean {
        return try {
            val capabilityInfo = capabilityClient.getCapability(
                CAPABILITY_MOBILE,
                CapabilityClient.FILTER_REACHABLE
            ).await()
            
            capabilityInfo.nodes.isNotEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "연결 확인 실패: ${e.message}")
            false
        }
    }

    /**
     * Data Layer API로 데이터 전송
     */
    private suspend fun sendToMobile(clientSecretKey: String, jsonString: String): Boolean {
        return try {
            // PutDataRequest 생성
            val putDataRequest = PutDataMapRequest.create("$PATH_WORKOUT_DATA/$clientSecretKey").apply {
                dataMap.putString("json", jsonString)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent() // 즉시 전송 시도
            
            // 전송
            val dataItem = dataClient.putDataItem(putDataRequest).await()
            
            Log.d(TAG, "✅ DataItem 전송됨: ${dataItem.uri}")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 전송 실패: ${e.message}", e)
            false
        }
    }

    /**
     * 전송 성공한 데이터를 DB에서 마킹
     */
    private suspend fun markAsSynced(
        heartRates: List<HeartRateRecordEntity>,
        locations: List<LocationRecordEntity>,
        cadences: List<CadenceRecordEntity>,
        calories: List<CalorieRecordEntity>,
        clientSecretKey: String
    ) {
        val dao = database.workoutDao()
        
        if (heartRates.isNotEmpty()) {
            dao.markHeartRatesAsSynced(heartRates.map { it.id }, clientSecretKey)
        }
        if (locations.isNotEmpty()) {
            dao.markLocationsAsSynced(locations.map { it.id }, clientSecretKey)
        }
        if (cadences.isNotEmpty()) {
            dao.markCadencesAsSynced(cadences.map { it.id }, clientSecretKey)
        }
        if (calories.isNotEmpty()) {
            dao.markCaloriesAsSynced(calories.map { it.id }, clientSecretKey)
        }
    }

    /**
     * 전체 미동기화 데이터 개수 조회
     */
    suspend fun getUnsyncedCount(): UnsyncedCount {
        val dao = database.workoutDao()
        return UnsyncedCount(
            heartRate = dao.getUnsyncedHeartRateCount(),
            location = dao.getUnsyncedLocationCount(),
            cadence = dao.getUnsyncedCadenceCount(),
            calorie = dao.getUnsyncedCalorieCount()
        )
    }

    // ==================== 변환 함수 ====================
    
    private fun convertToHeartRate(entity: HeartRateRecordEntity): HeartRateRecord {
        return HeartRateRecord(
            seq = entity.id,
            heartRate = entity.heartRate,
            createdAt = entity.timestamp
        )
    }
    
    private fun convertToGpsPoint(entity: LocationRecordEntity): GpsPoint {
        return GpsPoint(
            seq = entity.id,
            latitude = entity.latitude,
            longitude = entity.longitude,
            altitude = entity.altitude,
            speed = entity.speed,
            createdAt = entity.timestamp
        )
    }
    
    private fun convertToCadence(entity: CadenceRecordEntity): CadenceRecord {
        return CadenceRecord(
            seq = entity.id,
            cadence = entity.cadence.toDouble(),
            createdAt = entity.timestamp
        )
    }
}

// ==================== 데이터 모델 ====================

data class WorkoutDataBatch(
    val clientSecretKey: String,
    val sessionId: Long?,
    val userId: String,
    val startTime: Long,
    val endTime: Long,
    val summary: WorkoutSummary,
    val cadenceRecords: List<CadenceRecord>,
    val heartRateRecords: List<HeartRateRecord>,
    val gpsPoints: List<GpsPoint>
)

data class WorkoutSummary(
    val totalSteps: Int,
    val totalDistance: Double,
    val totalCalories: Int,
    val avgHeartRate: Int,
    val duration: Int,
    val avgCadence: Int,
    val avgPace: Int,
    val elevation: Double
)

data class CadenceRecord(
    val seq: Long,
    val cadence: Double,
    val createdAt: Long
)

data class HeartRateRecord(
    val seq: Long,
    val heartRate: Int,
    val createdAt: Long
)

data class GpsPoint(
    val seq: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val speed: Float?,
    val createdAt: Long
)

// ==================== 결과 타입 ====================

sealed class SyncResult {
    data class Success(
        val clientSecretKey: String,
        val heartRateCount: Int,
        val gpsCount: Int,
        val cadenceCount: Int
    ) : SyncResult()
    
    data class Queued(val clientSecretKey: String) : SyncResult()
    data object NoData : SyncResult()
    data class Error(val message: String) : SyncResult()
}

data class UnsyncedCount(
    val heartRate: Int,
    val location: Int,
    val cadence: Int,
    val calorie: Int
) {
    val total: Int get() = heartRate + location + cadence + calorie
}

