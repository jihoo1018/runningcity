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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
    
    private val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
    
    // 동시 실행 방지를 위한 Mutex (순서 보장 강화)
    private val syncMutex = Mutex()

    companion object {
        private const val TAG = "WatchDataSync"
        private const val DEFAULT_BATCH_SIZE = 300
        private const val PATH_WORKOUT_DATA = "/workout_data"
        private const val CAPABILITY_MOBILE = "running_city_mobile"
    }

    // Double, Float 값이 Infinity/NaN이면 0으로 치환
    private fun Double.safeOrZero(): Double =
        if (this.isNaN() || this.isInfinite()) 0.0 else this

    private fun Double?.safeOrZero(): Double =
        if (this == null || this.isNaN() || this.isInfinite()) 0.0 else this

    private fun Float?.safeOrZero(): Float =
        if (this == null || this.isNaN() || this.isInfinite()) 0f else this


    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
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

            // 1. 모바일 연결 확인
            val isMobileReachable = checkMobileConnection()
            Log.d(TAG, "모바일 연결 상태: $isMobileReachable")

            // 2. 세션 조회
            val session = watchSessionId?.let {
                database.workoutDao().getSession(it)
            } ?: run {
                Log.e(TAG, "❌ 세션 정보를 찾을 수 없습니다: $watchSessionId")
                return SyncResult.Error("세션 정보를 찾을 수 없습니다")
            }

            val sessionSeq = session.seq
            Log.d(TAG, "✅ 세션 조회 성공: seq=$sessionSeq, clientSecretKey=${session.clientSecretKey}")

            Log.d(
                TAG,
                """
            🔍 [SESSION SUMMARY CHECK]
            - totalSteps      = ${session.totalSteps}
            - totalDistance   = ${session.totalDistance}
            - totalCalories   = ${session.totalCalories}
            - avgHeartRate    = ${session.avgHeartRate}
            - duration        = ${session.duration}
            - avgCadence      = ${session.avgCadence}
            - elevation       = ${session.elevation}
            """.trimIndent()
            )

            // 3. 미동기화 데이터 조회
            val heartRates =
                database.workoutDao().getUnsyncedHeartRates(batchSize)
                    .filter { it.workoutSessionSeq == sessionSeq }

            val locations =
                database.workoutDao().getUnsyncedLocations(batchSize)
                    .filter { it.workoutSessionSeq == sessionSeq }

            val cadences =
                database.workoutDao().getUnsyncedCadences(batchSize)
                    .filter { it.workoutSessionSeq == sessionSeq }

            val calories =
                database.workoutDao().getUnsyncedCalories(batchSize)
                    .filter { it.workoutSessionSeq == sessionSeq }

            Log.d(
                TAG,
                """
            📊 배치 전송 준비 (세션: $watchSessionId)
            - 심박수: ${heartRates.size}개
            - GPS: ${locations.size}개
            - 케이던스: ${cadences.size}개
            - 칼로리: ${calories.size}개
            """.trimIndent()
            )

            if (heartRates.isEmpty() && locations.isEmpty() &&
                cadences.isEmpty() && calories.isEmpty()
            ) {
                Log.d(TAG, "전송할 데이터 없음")
                return SyncResult.NoData
            }

            // --------------------------------------------------------------------
            // ⭐⭐ 4. Summary 값을 DB 기록 기반으로 재계산 (핵심 수정 부분)
            // --------------------------------------------------------------------

            // 총 거리 계산 (GPS 기반)
            val totalDistanceCalced =
                if (locations.size > 1) {
                    locations.zipWithNext { a, b ->
                        haversine(a.latitude, a.longitude, b.latitude, b.longitude)
                    }.sum()
                } else 0.0

            // 총 칼로리 증가량
            val totalCaloriesCalced = calories.sumOf { it.calories.toDouble() }

            // 총 스텝 (cadence 하나당 발생 스텝 수의 단순 합)
            val totalStepsCalced = session.totalSteps

            // 평균 심박수
            val avgHeartRateCalced =
                heartRates.map { it.heartRate }.average().takeIf { !it.isNaN() }?.toInt() ?: 0

            // 평균 케이던스
            val avgCadenceCalced =
                cadences.map { it.cadence }.average().takeIf { !it.isNaN() }?.toInt() ?: 0

            // 지속시간 (startTime~endTime)
            val durationCalced =
                ((session.endTime!! - session.startTime) / 1000).toInt()

            // 평균 고도
            var avgElevationCalced =
                locations.mapNotNull { it.altitude }
                    .average()

            if (avgElevationCalced.isNaN() || avgElevationCalced.isInfinite())
                avgElevationCalced = 0.0

            // 평균 페이스 (초/km)
            val avgPaceCalced =
                if (totalDistanceCalced > 0)
                    ((durationCalced / (totalDistanceCalced / 1000))).toInt()
                else 0

            Log.d(
                TAG, """
            📊 Recalculated SUMMARY
            - distance   = $totalDistanceCalced
            - calories   = $totalCaloriesCalced
            - steps      = $totalStepsCalced
            - hr         = $avgHeartRateCalced
            - cadence    = $avgCadenceCalced
            - duration   = $durationCalced
            - pace       = $avgPaceCalced
            - elevation  = $avgElevationCalced
        """.trimIndent()
            )

            // --------------------------------------------------------------------
            // 5. WorkoutDataBatch 생성 (수정된 summary 적용)
            // --------------------------------------------------------------------

            val isMobileStarted = session.sessionId > 0

            val batch = WorkoutDataBatch(
                clientSecretKey = if (isMobileStarted) "" else session.clientSecretKey,
                sessionId = if (isMobileStarted) session.sessionId else null,
                userId = session.userId,
                startTime = session.startTime,
                endTime = session.endTime ?: System.currentTimeMillis(),
                summary = WorkoutSummary(
                    totalSteps = totalStepsCalced,
                    totalDistance = totalDistanceCalced,
                    totalCalories = totalCaloriesCalced.toInt(),
                    avgHeartRate = avgHeartRateCalced,
                    duration = durationCalced,
                    avgCadence = avgCadenceCalced,
                    avgPace = avgPaceCalced,
                    elevation = avgElevationCalced
                ),
                cadenceRecords = cadences.map { convertToCadence(it) },
                heartRateRecords = heartRates.map { convertToHeartRate(it) },
                gpsPoints = locations.map { convertToGpsPoint(it) }
            )

            val jsonString = gson.toJson(batch)

            Log.d(TAG, "📤 전송할 JSON 데이터:\n$jsonString")

            // 6. 모바일로 전송
            val success = sendToMobile(session.clientSecretKey, jsonString)

            if (success) {
                try {
                    markAsSynced(heartRates, locations, cadences, calories, session.clientSecretKey)

                    SyncResult.Success(
                        clientSecretKey = session.clientSecretKey,
                        heartRateCount = heartRates.size,
                        gpsCount = locations.size,
                        cadenceCount = cadences.size
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ DB 업데이트 실패: ${e.message}")
                    SyncResult.Error("DB update failed: ${e.message}")
                }
            } else {
                Log.w(TAG, "⚠️ 전송 실패 - 다음에 재시도")
                SyncResult.Queued(session.clientSecretKey)
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
            // Capability로 연결된 노드 확인
            val capabilityInfo = capabilityClient.getCapability(
                CAPABILITY_MOBILE,
                CapabilityClient.FILTER_REACHABLE
            ).await()

            // 직접 연결된 노드도 확인
            val connectedNodes = nodeClient.connectedNodes.await()

            Log.d(TAG, """
                📡 연결 상태 확인:
                - Capability 노드: ${capabilityInfo.nodes.size}개
                - 직접 연결 노드: ${connectedNodes.size}개
                - 노드 목록: ${connectedNodes.map { it.displayName }}
            """.trimIndent())

            val isConnected = capabilityInfo.nodes.isNotEmpty() || connectedNodes.isNotEmpty()

            if (!isConnected) {
                Log.w(TAG, """
                    ⚠️ 모바일 연결 안 됨
                    - 블루투스가 켜져있는지 확인
                    - 모바일과 워치가 페어링되어 있는지 확인
                    - 모바일 앱이 설치되어 있는지 확인
                    - WiFi는 필요 없음 (블루투스만 사용)
                """.trimIndent())
            }

            isConnected
        } catch (e: Exception) {
            Log.w(TAG, "연결 확인 실패: ${e.message}")
            false
        }
    }

    /**
     * Data Layer API로 데이터 전송
     * 
     * ⚠️ 중요: Data Layer API는 블루투스만 사용 (WiFi 불필요)
     * - 모바일과 워치가 페어링되어 있어야 함
     * - 블루투스가 켜져있어야 함
     * - 모바일 앱이 설치되어 있어야 함
     * - ADB 연결은 개발용이며 실제 사용에는 불필요
     */
    private suspend fun sendToMobile(clientSecretKey: String, jsonString: String): Boolean {
        return try {
            // 연결 상태 먼저 확인
            val connectedNodes = nodeClient.connectedNodes.await()
            Log.d(TAG, """
                📤 데이터 전송 시도
                - 연결된 노드: ${connectedNodes.size}개
                - 노드 목록: ${connectedNodes.map { "${it.displayName} (${it.id})" }}
            """.trimIndent())
            
            if (connectedNodes.isEmpty()) {
                Log.w(TAG, """
                    ⚠️ 연결된 노드 없음 - Data Layer 큐에 저장됨
                    - 모바일이 연결되면 자동으로 전송됨
                    - 블루투스 연결 확인 필요
                """.trimIndent())
            }
            
            // PutDataRequest 생성
            val putDataRequest = PutDataMapRequest.create("$PATH_WORKOUT_DATA/$clientSecretKey").apply {
                dataMap.putString("json", jsonString)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent() // 즉시 전송 시도
            
            // 전송 (연결 안 되어있어도 큐에 저장됨)
            val dataItem = dataClient.putDataItem(putDataRequest).await()
            
            Log.d(TAG, """
                ✅ DataItem 전송 완료
                - URI: ${dataItem.uri}
                - 연결 상태: ${if (connectedNodes.isNotEmpty()) "연결됨" else "큐에 저장됨"}
            """.trimIndent())
            true
            
        } catch (e: Exception) {
            Log.e(TAG, """
                ❌ 전송 실패: ${e.message}
                - 연결 상태 확인 필요
                - 블루투스 켜져있는지 확인
            """.trimIndent(), e)
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
            dao.markHeartRatesAsSynced(heartRates.map { it.seq }, clientSecretKey)
        }
        if (locations.isNotEmpty()) {
            dao.markLocationsAsSynced(locations.map { it.seq }, clientSecretKey)
        }
        if (cadences.isNotEmpty()) {
            dao.markCadencesAsSynced(cadences.map { it.seq }, clientSecretKey)
        }
        if (calories.isNotEmpty()) {
            dao.markCaloriesAsSynced(calories.map { it.seq }, clientSecretKey)
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
            seq = entity.seq,
            heartRate = entity.heartRate,
            createdAt = entity.createdAt
        )
    }
    
    private fun convertToGpsPoint(entity: LocationRecordEntity): GpsPoint {
        return GpsPoint(
            seq = entity.seq,
            latitude = entity.latitude.safeOrZero(),             // 혹시 모를 값 방어
            longitude = entity.longitude.safeOrZero(),
            altitude = 0.0,            // ✨ 여기
            speed = entity.speed.safeOrZero(),                   // ✨ 여기 (Float)
            createdAt = entity.createdAt
        )
    }
    
    private fun convertToCadence(entity: CadenceRecordEntity): CadenceRecord {
        return CadenceRecord(
            seq = entity.seq,
            cadence = entity.cadence.toDouble(),
            createdAt = entity.createdAt
        )
    }
}

// ==================== 데이터 모델 ====================

data class WorkoutDataBatch(
    @com.google.gson.annotations.SerializedName("clientSecretKey")
    val clientSecretKey: String,
    @com.google.gson.annotations.SerializedName("sessionId")
    val sessionId: Long?,
    @com.google.gson.annotations.SerializedName("userId")
    val userId: String,
    @com.google.gson.annotations.SerializedName("startTime")
    val startTime: Long,
    @com.google.gson.annotations.SerializedName("endTime")
    val endTime: Long,
    @com.google.gson.annotations.SerializedName("summary")
    val summary: WorkoutSummary,
    @com.google.gson.annotations.SerializedName("cadenceRecords")
    val cadenceRecords: List<CadenceRecord>,
    @com.google.gson.annotations.SerializedName("heartRateRecords")
    val heartRateRecords: List<HeartRateRecord>,
    @com.google.gson.annotations.SerializedName("gpsPoints")
    val gpsPoints: List<GpsPoint>
)

data class WorkoutSummary(
    @com.google.gson.annotations.SerializedName("totalSteps")
    val totalSteps: Int,
    @com.google.gson.annotations.SerializedName("totalDistance")
    val totalDistance: Double,
    @com.google.gson.annotations.SerializedName("totalCalories")
    val totalCalories: Int,
    @com.google.gson.annotations.SerializedName("avgHeartRate")
    val avgHeartRate: Int,
    @com.google.gson.annotations.SerializedName("duration")
    val duration: Int,
    @com.google.gson.annotations.SerializedName("avgCadence")
    val avgCadence: Int,
    @com.google.gson.annotations.SerializedName("avgPace")
    val avgPace: Int,
    @com.google.gson.annotations.SerializedName("elevation")
    val elevation: Double
)

data class CadenceRecord(
    @com.google.gson.annotations.SerializedName("seq")
    val seq: Long,
    @com.google.gson.annotations.SerializedName("cadence")
    val cadence: Double,
    @com.google.gson.annotations.SerializedName("createdAt")
    val createdAt: Long
)

data class HeartRateRecord(
    @com.google.gson.annotations.SerializedName("seq")
    val seq: Long,
    @com.google.gson.annotations.SerializedName("heartRate")
    val heartRate: Int,
    @com.google.gson.annotations.SerializedName("createdAt")
    val createdAt: Long
)

data class GpsPoint(
    @com.google.gson.annotations.SerializedName("seq")
    val seq: Long,
    @com.google.gson.annotations.SerializedName("latitude")
    val latitude: Double,
    @com.google.gson.annotations.SerializedName("longitude")
    val longitude: Double,
    @com.google.gson.annotations.SerializedName("altitude")
    val altitude: Double?,
    @com.google.gson.annotations.SerializedName("speed")
    val speed: Float?,
    @com.google.gson.annotations.SerializedName("createdAt")
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

