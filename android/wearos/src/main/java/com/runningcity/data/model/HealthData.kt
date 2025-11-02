package com.runningcity.data.model

/**
 * 러닝시티 센서 데이터 모델
 * Android Health Services에서 제공하는 순수 데이터만 수집
 */

// ============================================
// 1️⃣ 심박수 데이터
// ============================================
data class HeartRateData(
    val bpm: Int,                    // 심박수 (beats per minute)
    val timestamp: Long,             // 측정 시간 (밀리초)
    val accuracy: String             // 정확도 ("HIGH", "MEDIUM", "LOW", "UNKNOWN")
)

// ============================================
// 2️⃣ GPS 위치 데이터
// ============================================
data class LocationData(
    val latitude: Double,            // 위도
    val longitude: Double,           // 경도
    val altitude: Double,            // 고도 (미터)
    val bearing: Float,              // 방향 (0~360도, 북쪽이 0)
    val speed: Float,                // 속도 (m/s)
    val accuracy: Float,             // 정확도 (미터)
    val timestamp: Long
)

// ============================================
// 3️⃣ 걸음 수 데이터
// ============================================
data class StepsData(
    val steps: Long,                 // 총 걸음 수
    val timestamp: Long
)

// ============================================
// 4️⃣ 거리 데이터
// ============================================
data class DistanceData(
    val totalMeters: Double,         // 총 거리 (미터)
    val timestamp: Long
)

// ============================================
// 5️⃣ 칼로리 데이터
// ============================================
data class CaloriesData(
    val totalCalories: Double,       // 총 칼로리 (kcal)
    val timestamp: Long
)

// ============================================
// 6️⃣ 속도 데이터
// ============================================
data class SpeedData(
    val metersPerSecond: Double,     // 속도 (m/s)
    val timestamp: Long
)

// ============================================
// 7️⃣ 케이던스 데이터 (분당 걸음 수)
// ============================================
data class CadenceData(
    val stepsPerMinute: Double,      // 분당 걸음 수 (spm)
    val timestamp: Long
)

// ============================================
// 8️⃣ 페이스 데이터 (km당 시간)
// ============================================
data class PaceData(
    val minutesPerKm: Double,        // km당 소요 시간 (분)
    val timestamp: Long
)

// ============================================
// 9️⃣ 고도 변화 데이터
// ============================================
data class ElevationData(
    val gain: Double,                // 상승 고도 (미터)
    val loss: Double,                // 하강 고도 (미터)
    val timestamp: Long
)

// ============================================
// 🔟 통합 운동 데이터 (실시간 스냅샷)
// ============================================
data class WorkoutSnapshot(
    val heartRate: HeartRateData?,
    val location: LocationData?,
    val steps: StepsData?,
    val distance: DistanceData?,
    val calories: CaloriesData?,
    val speed: SpeedData?,
    val cadence: CadenceData?,
    val pace: PaceData?,
    val elevation: ElevationData?,
    val timestamp: Long
) {
    /**
     * 백엔드 전송용 Map 변환
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "heartRate" to heartRate?.bpm,
            "heartRateAccuracy" to heartRate?.accuracy,
            "latitude" to location?.latitude,
            "longitude" to location?.longitude,
            "altitude" to location?.altitude,
            "bearing" to location?.bearing,
            "locationSpeed" to location?.speed,
            "locationAccuracy" to location?.accuracy,
            "steps" to steps?.steps,
            "distance" to distance?.totalMeters,
            "calories" to calories?.totalCalories,
            "speed" to speed?.metersPerSecond,
            "cadence" to cadence?.stepsPerMinute,
            "pace" to pace?.minutesPerKm,
            "elevationGain" to elevation?.gain,
            "elevationLoss" to elevation?.loss,
            "timestamp" to timestamp
        )
    }
}

// ============================================
// 1️⃣1️⃣ 운동 세션 정보
// ============================================
data class WorkoutSession(
    val sessionId: String,           // 세션 고유 ID
    val userId: String,              // 사용자 ID
    val exerciseType: String,        // 운동 타입 (예: "RUNNING")
    val startTime: Long,             // 시작 시간
    val endTime: Long?,              // 종료 시간 (null이면 진행 중)
    val snapshots: List<WorkoutSnapshot>, // 실시간 데이터 스냅샷들
    val isActive: Boolean            // 현재 활성화 상태
) {
    /**
     * 총 운동 시간 (초)
     */
    fun getDurationSeconds(): Long {
        return if (endTime != null) {
            (endTime - startTime) / 1000
        } else {
            (System.currentTimeMillis() - startTime) / 1000
        }
    }

    /**
     * 포맷팅된 운동 시간 (예: "25:30")
     */
    fun getFormattedDuration(): String {
        val totalSeconds = getDurationSeconds()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    /**
     * 최종 통계 데이터
     */
    fun getFinalStats(): Map<String, Any?> {
        val lastSnapshot = snapshots.lastOrNull()
        val avgHeartRate = snapshots
            .mapNotNull { it.heartRate?.bpm }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toInt()

        return mapOf(
            "sessionId" to sessionId,
            "userId" to userId,
            "exerciseType" to exerciseType,
            "startTime" to startTime,
            "endTime" to endTime,
            "duration" to getDurationSeconds(),
            "totalDistance" to lastSnapshot?.distance?.totalMeters,
            "totalSteps" to lastSnapshot?.steps?.steps,
            "totalCalories" to lastSnapshot?.calories?.totalCalories,
            "averageHeartRate" to avgHeartRate,
            "averageCadence" to snapshots
                .mapNotNull { it.cadence?.stepsPerMinute }
                .takeIf { it.isNotEmpty() }
                ?.average(),
            "averageSpeed" to snapshots
                .mapNotNull { it.speed?.metersPerSecond }
                .takeIf { it.isNotEmpty() }
                ?.average(),
            "maxHeartRate" to snapshots
                .mapNotNull { it.heartRate?.bpm }
                .maxOrNull(),
            "elevationGain" to lastSnapshot?.elevation?.gain,
            "elevationLoss" to lastSnapshot?.elevation?.loss
        )
    }
}

// ============================================
// 1️⃣2️⃣ 운동 상태 (앱 내부 상태 관리용)
// ============================================
enum class ExerciseState {
    IDLE,           // 대기 중
    PREPARING,      // 준비 중 (센서 초기화)
    ACTIVE,         // 운동 중
    PAUSED,         // 일시정지
    ENDED           // 종료됨
}

// ============================================
// 1️⃣3️⃣ 센서 상태 (디버깅용)
// ============================================
data class SensorStatus(
    val heartRateAvailable: Boolean,    // 심박수 센서 사용 가능
    val gpsAvailable: Boolean,          // GPS 사용 가능
    val gpsAccuracy: String,            // GPS 정확도 상태
    val timestamp: Long
)